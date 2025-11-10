package com.ltl.ltl.service.syntax;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SyntaxCheckerTest {
    
    
    
    private final SyntaxChecker checker = new SyntaxChecker();
    
    @Test
    void manyValidFormulasReturnTrue() {
        String[] formulas = {
            "a", "b", "c", "Ga", "Xa", "Fa", "Wa", "GGXFa", "GXFWa",
            "aUb", "aRb", "a&b", "a|b", "a>b", "a<b", "a=b",
            "a&b|c", "a|b&c", "(a>b)>c", "(a=b)=c", "(a<b)<c",
            "(a)", "(aUb)", "((a))", "(aU(bRc))",
            "G(aUb)", "F(G(a&b))", "X(G(F(a)))", "W(G(a)|F(b))",
            "G( a & (b | Xc) )", "(G(a))U(F(a > b))", "(aUb)R(Gc)",
            "  a  ", " ( a U ( b ) ) ", "G(  a  &  ( b  |  Xc ) )",
            "((((a>b)>c)>b)>c)>a", "((a=b)=c)=b", "((a<b)<c)<b",
            "((aUb)U(cUb))", "((aRb)R(cRb))", "aU(bRc)",
            "G(a)=F(b)", "a>(b|c)", "((a&b)>(c|b))", "GFXWa", "G(F(X(a)))",
            "G( (a U (Gb)) & (F(c) | X( a R (b U c) )) )",
            // new complex mixes involving new boolean/implication ops (restricted to a,b,c)
            "(a&b)>(b=c)", "(a|b)<(c&a)", "(a=b)=(b=c)", "(a>b)=(b>c)",
            // chains combining temporal and boolean
            "G(a)=F(b|c)", "F(a&b)>X(c=a)", "(aU(b|c))>(aRb)",
            // nested equivalence with implication
            "(a=b)>(c=a)", "(a>b)=(c<a)"
        };
        for (String formula : formulas) {
            assertTrue(checker.isValid(formula), "Should be valid: " + formula);
        }
    }

    @Test
    void emptyStringIsInvalid() {
        assertFalse(checker.isValid(""));
    }

    @Test
    void nullIsInvalid() {
        assertFalse(checker.isValid(null));
    }

    @Test
    void missingOperandIsInvalid() {
        String[] formulas = {"G", "X", "F", "W", "aU", "aR", "a&", "a|", "a>", "a<", "a="};
        for (String formula : formulas) {
            assertFalse(checker.isValid(formula), "Should be invalid: " + formula);
        }
    }

    @Test
    void badParenthesisIsInvalid() {
        String[] formulas = {"(a", "a)", "(aUb", "aU(b", "(a&b))"};
        for (String formula : formulas) {
            assertFalse(checker.isValid(formula), "Should be invalid: " + formula);
        }
    }

    @Test
    void invalidTokensAreInvalid() {
        String[] formulas = {"Z", "Z a", "a && b", "a || b", "a -> b", "a < > b", "1", "a U", "U b"};
        for (String formula : formulas) {
            assertFalse(checker.isValid(formula), "Should be invalid: " + formula);
        }
    }

    @Test
    void juxtaposedAtomsAreInvalid() {
        String[] formulas = {"ab", "a b", "a (b)", "(a)b"};
        for (String formula : formulas) {
            assertFalse(checker.isValid(formula), "Should be invalid: " + formula);
        }
    }

    @Test
    void operatorSequencesAreInvalid() {
        String[] formulas = {"a&&b", "a||b", "a==b", "a>>b", "a<<b"};
        for (String formula : formulas) {
            assertFalse(checker.isValid(formula), "Should be invalid: " + formula);
        }
    }

    @Test
    void malformedComplexIsInvalid() {
        String[] formulas = {"F(a > )", "G()", "( )"};
        for (String formula : formulas) {
            assertFalse(checker.isValid(formula), "Should be invalid: " + formula);
        }
    }


    @ParameterizedTest(name = "valid[{index}] => {0}")
    @ValueSource(strings = {
        // atoms
        "a", "b", "c",
        // unary chains
        "Ga", "Xa", "Fa", "Wa", "GGXFa", "GXFWa",
        // binary basics
        "aUb", "aRb", "a&b", "a|b", "a>b", "a<b", "a=b",
        // precedence & associativity
        "a&b|c", "a|b&c", "a>b>c", "a=b=c", "a<b<c",
        // parentheses variations
        "(a)", "(aUb)", "((a))", "(aU(bRc))",
        // nesting with unary
        "G(aUb)", "F(G(a&b))", "X(G(F(a)))", "W(G(a)|F(b))",
        // complex mix
        "G( a & (b | Xc) )", "(G(a))U(F(a > b))", "(aUb)R(Gc)",
        // whitespace stress
        "  a  ", " ( a U ( b ) ) ", "G(  a  &  ( b  |  Xc ) )",
        // right-associative chains
        "a>b>c>b", "a=b=c=b", "a<b<c<b",
        // until / release combos
        "(aUb)U(cUb)", "(aRb)R(cRb)", "aU(bRc)",
        // equivalence with other ops
        "G(a)=F(b)", "a>(b|c)", "(a&b)>(c|b)",
    // mixed prefix inside
    "GFXWa", "G(F(X(a)))"
    })
    void validFormulasReturnTrue(String formula) {
        assertTrue(checker.isValid(formula), () -> "Expected valid: " + formula);
        assertDoesNotThrow(() -> checker.validateOrThrow(formula));
    }

    @ParameterizedTest(name = "invalid[{index}] => {0}")
    @ValueSource(strings = {
            // empty / null-like
            "",
            // missing operands
            "G", "X", "F", "W", "aU", "aR", "a&", "a|", "a>", "a<", "a=",
            // bad parenthesis
            "(a", "a)", "(aUb", "aU(b", "(a&b))",
            // invalid tokens
            "Z", "Z a", "a && b", "a || b", "a -> b", "a < > b", "1", "a U", "U b",
            // juxtaposed atoms without operator
            "ab", "a b", "a (b)", "(a)b",
            // operator sequences
            "a&&b", "a||b", "a==b", "a>>b", "a<<b",
            // malformed complex
            "F(a > )", "G()", "( )"
    })
    void invalidFormulasReturnFalse(String formula) {
        assertFalse(checker.isValid(formula), () -> "Expected invalid: " + formula);
    }

    @Test
    @DisplayName("null input -> isValid false and validateOrThrow throws with position 0")
    void nullInput() {
        assertFalse(checker.isValid(null));
        ParseException ex = assertThrows(ParseException.class, () -> checker.validateOrThrow(null));
        assertEquals(0, ex.position);
        assertTrue(ex.getMessage().contains("Input is null"));
    }

    @Test
    void complexDeeplyNestedStillValid() {
        String f = "G( (a U (Gb)) & (F(c) | X( a R (b U c) )) )";
        assertTrue(checker.isValid(f));
    }

    @Test
    void longRightAssociativeChainsValid() {
    String f = "((((a>b)>c)>b)>c)>a";
        assertTrue(checker.isValid(f));
        assertDoesNotThrow(() -> checker.validateOrThrow(f));
    }


    
@org.junit.jupiter.params.ParameterizedTest(name = "until/and/or precedence valid → {0}")
@org.junit.jupiter.params.provider.ValueSource(strings = {
    "aUb&c",            // (a U b) & c
    "a&(bUc)",          // a & (b U c)
    "(aUb)&(bUc)",
    "aR(b&c)|Ga",       // (a R (b & c)) | (G a)
    "G(aUb)&F(bRc)",    // (G (a U b)) & (F (b R c))
    "GXFaUb",           // ((G (X (F a))) U b)
    "G(a)U(b)"          // mit Klammern um Operanden
})
void untilHasHigherPrecedenceThanAndOr(String f) {
    assertTrue(checker.isValid(f), f);
    assertDoesNotThrow(() -> checker.validateOrThrow(f));
}

@org.junit.jupiter.params.ParameterizedTest(name = "U/R left-assoc valid → {0}")
@org.junit.jupiter.params.provider.ValueSource(strings = {
    "aUbUc",            // (a U b) U c
    "aRbRc",            // (a R b) R c
    "(aUb)Uc",
    "((aUb)Uc)Ub"
})
void untilAndReleaseAreLeftAssociative(String f) {
    assertTrue(checker.isValid(f), f);
    assertDoesNotThrow(() -> checker.validateOrThrow(f));
}

@org.junit.jupiter.params.ParameterizedTest(name = ">,<,= right-assoc valid → {0}")
@org.junit.jupiter.params.provider.ValueSource(strings = {
    "a>b>c>b",          // a > (b > (c > b))
    "a=b=c=b",          // a = (b = (c = b))
    "a<b<c<b"           // a < (b < (c < b))
})
void implicationEquivalenceRightAssociative(String f) {
    assertTrue(checker.isValid(f), f);
    assertDoesNotThrow(() -> checker.validateOrThrow(f));
}

@org.junit.jupiter.params.ParameterizedTest(name = "prefix chains valid → {0}")
@org.junit.jupiter.params.provider.ValueSource(strings = {
    "GGGGa",
    "XGXFb",
    "WGa",
    "GXF(Ga)"
})
void prefixChains(String f) {
    assertTrue(checker.isValid(f), f);
    assertDoesNotThrow(() -> checker.validateOrThrow(f));
}

@org.junit.jupiter.params.ParameterizedTest(name = "whitespace valid → {0}")
@org.junit.jupiter.params.provider.ValueSource(strings = {
    "G( a\t&\t(b | Xc) )",
    "(\t a \n U \n ( b ) \t)\n",
    "  a  \n&\n  ( b  |  Xc )"
})
void whitespaceAndNewlines(String f) {
    assertTrue(checker.isValid(f), f);
    assertDoesNotThrow(() -> checker.validateOrThrow(f));
}

@org.junit.jupiter.params.ParameterizedTest(name = "more invalid → {0}")
@org.junit.jupiter.params.provider.ValueSource(strings = {
    "a()",          // leere Klammern nach Atom
    "(a)(b)",       // zwei Primär-Ausdrücke ohne Operator
    "a ( ) b",      // leerer Ausdruck zwischen Operanden
    "G|a",          // Operator an falscher Stelle
    "a|&b",         // Operator-Sequenz
    "Ga)",          // schließende Klammer zu viel
    "((a)",         // schließende Klammer fehlt
    "(a) b",        // Juxtaposition ohne Operator
    "a U( )",       // leerer rechter Operand
    "W()",          // W ohne Argument
    "G(F())"        // verschachtelt leer
})
void moreInvalids(String f) {
    assertFalse(checker.isValid(f), f);
    assertThrows(ParseException.class, () -> checker.validateOrThrow(f));
}

@org.junit.jupiter.api.Test
@org.junit.jupiter.api.DisplayName("Until vs And precedence in both directions")
void untilVsAndBothWays() {
    assertTrue(checker.isValid("aUb&c"));        // (aUb)&c
    assertTrue(checker.isValid("a&(bUc)"));      // a&(bUc)
    assertTrue(checker.isValid("(aUb)&(bUc)"));  // explizit geklammert
}
}