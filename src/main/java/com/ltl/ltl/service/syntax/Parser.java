package com.ltl.ltl.service.syntax;

public class Parser {
    private final Lexer lx;
    private Token lookahead;

    public Parser(Lexer lx) throws ParseException {
        this.lx = lx;
        this.lookahead = lx.next();
    }

    public void parseFormula() throws ParseException {
        parseEquiv();
        expect(TokenType.EOF); // alles muss konsumiert sein
    }

    // equiv := impl ( '=' impl )*
    private void parseEquiv() throws ParseException {
        parseImpl();
        while (lookahead.getType() == TokenType.EQUIV) {
            consume(TokenType.EQUIV);
            parseImpl();
        }
    }

    // impl := orExpr ( ('>' | '<') orExpr )*
    private void parseImpl() throws ParseException {
        parseOr();
        while (lookahead.getType() == TokenType.IMPL || lookahead.getType() == TokenType.RIMPL) {
            consume(lookahead.getType());
            parseOr();
        }
    }

    // orExpr := andExpr ( '|' andExpr )*
    private void parseOr() throws ParseException {
        parseAnd();
        while (lookahead.getType() == TokenType.OR) {
            consume(TokenType.OR);
            parseAnd();
        }
    }

    // andExpr := untilExpr ( '&' untilExpr )*
    private void parseAnd() throws ParseException {
        parseUntilRelease();
        while (lookahead.getType() == TokenType.AND) {
            consume(TokenType.AND);
            parseUntilRelease();
        }
    }

    // untilExpr := prefix ( ('U' | 'R') prefix )*
    private void parseUntilRelease() throws ParseException {
        parsePrefix();
        while (lookahead.getType() == TokenType.U || lookahead.getType() == TokenType.R) {
            consume(lookahead.getType());
            parsePrefix();
        }
    }

    // prefix := (G|X|F|W)* primary
    private void parsePrefix() throws ParseException {
        while (lookahead.getType() == TokenType.G ||
               lookahead.getType() == TokenType.NOT ||
               lookahead.getType() == TokenType.X ||
               lookahead.getType() == TokenType.F ||
               lookahead.getType() == TokenType.W) {
            consume(lookahead.getType());
        }
        parsePrimary();
    }

    // primary := PROP | '(' equiv ')'
    private void parsePrimary() throws ParseException {
        switch (lookahead.getType()) {
            case PROP -> consume(TokenType.PROP);
            case LPAREN -> {
                consume(TokenType.LPAREN);
                parseEquiv();
                expect(TokenType.RPAREN);
            }
            default -> throw error("Atom (a|b|c) oder '(' erwartet, gefunden: " + lookahead);
        }
    }

    // --- utils ---
    private void consume(TokenType expected) throws ParseException {
        if (lookahead.getType() != expected) {
            throw error("Erwartet: " + expected + ", aber gefunden: " + lookahead);
        }
        lookahead = lx.next();
    }

    void expect(TokenType expected) throws ParseException {
        consume(expected);
    }

    private ParseException error(String msg) {
        return new ParseException(msg, lookahead.getPos());
    }
}
