<div align="center">

# LTL Tableau API (Spring Boot) + Parser/NNF

Build and explore tableau trees for Linear Temporal Logic (LTL) formulas via a Spring Boot REST API. Includes a lexer/parser, NNF transformer, and a tableau constructor with simple loop handling.

Supported atomic propositions: `a`, `b`, `c`. Operators: `G X F W U R ! & | > < = ( )`.

</div>

## Features
- Lexer with precise error locations; `ParseException` reports 0-based positions
- Two parsing layers:
	- `SyntaxChecker` – fast validation
	- `AstParser` – builds `IFormula` AST nodes
- NNF conversion (`Nnf`) automatically applied inside tableau entries
- Tableau construction with expansion rules (G, F, X, U, R, &, |) and basic loop detection over next-steps
- REST endpoint to materialize the tableau as JSON, including per-formula “checked” flags

## Project layout (selected)
```
src/main/java/com/ltl/ltl/
	controller/
		TableauController.java       # GET /api/tableau/tree
		TableauService.java          # parse -> build tableau -> map to DTO
	ltl/web/dto/
		TableauNodeDto.java          # JSON shape for a tableau node
	model/
		IFormula.java AtomicProposition.java Symbol.java
		formulas/                    # AST nodes (Next, Globally, Finally, WeakNext, Until, Release, And, Or, Not, ...)
		tableau/                     # TableauEntry (NNF+checked), TableauNode (tree)
	service/
		AstParser.java StringParser.java
		tableau/                     # TableauProof, TableauTreeConstruction
		syntax/                      # Lexer, Parser, SyntaxChecker, Nnf, Token*, ParseException
```

## Run and test (Windows PowerShell)
```powershell
mvn clean test
mvn spring-boot:run
```

The app starts on http://localhost:8080.

## REST API
Build a tableau tree for a formula.

- Method: GET
- URL: `/api/tableau/tree?formula=...`

Important: URL-encode reserved characters in the query string.
- `&` → `%26`
- `|` → `%7C`
- `>` → `%3E`
- `<` → `%3C`
- `=` → `%3D`

Examples
- G(G(a))
	- http://localhost:8080/api/tableau/tree?formula=G(G(a))
- G(F(a)) | a
	- http://localhost:8080/api/tableau/tree?formula=G(F(a))%7Ca
- G(F(a)) & a
	- http://localhost:8080/api/tableau/tree?formula=G(F(a))%26a
- G(a > X(G(a)))
	- http://localhost:8080/api/tableau/tree?formula=G(a%3EX(G(a)))

Tip: For complex inputs, consider a POST endpoint with a JSON body to avoid manual encoding.

### JSON shape
Each node in the returned tree contains:
- `formulas: string[]` – formulas at this node (stringified)
- `entries: { formula: string, checked: boolean }[]` – each formula with its checked state
- `closed: true | false | null` – leaf acceptance (true), contradiction/dead-end (false), or undecided for internal nodes (null)
- `children: TableauNodeDto[]` – child nodes (may be empty)

Semantics
- Parents with children have `closed = null` (undecided). Only leaves carry `true` or `false`.
- NNF is applied automatically to all entries; negations are pushed inside.
- Basic loop detection prevents infinite unfolding on next-steps.

## Grammar (informal)
```
formula      := equiv
equiv        := impl ( '=' impl )*
impl         := rimpl ( '>' rimpl )*
rimpl        := until ( '<' until )*
until        := release ( 'U' release )*
release      := orExpr ( 'R' orExpr )*
orExpr       := andExpr ( '|' andExpr )*
andExpr      := unary ( '&' unary )*
unary        := ( 'G' | 'X' | 'F' | 'W' | '!' )* primary
primary      := ATOM | '(' formula ')'
ATOM         := 'a' | 'b' | 'c'
```

## Examples (parser/AST)
```java
AstParser p = new AstParser(new Lexer("G(a U (b|Xc))"));
IFormula f = p.parseFormula();
```

## Limitations
- Only atoms `a`, `b`, `c` are recognized (extend `Lexer` to add more)
- URL-encoding is required for reserved characters if you use GET
- Generated trees can grow quickly; large formulas may produce big JSON

## Development notes
- Loop/closure logic lives in `TableauProof` and `TableauTreeConstruction`
- Each `TableauEntry` normalizes to NNF on construction and carries a `checked` flag
- DTO mapping includes `entries[].checked` to aid debugging/visualization

## Contributing
```powershell
mvn -q test
```
Add/adjust tests for grammar or tableau rule changes and open a PR.

