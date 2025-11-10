package com.ltl.ltl.service.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for NNF transformation covering all supported operators. */
public class NnfTest {

    private final Nnf nnf = new Nnf();

    private String nnfOf(String input) throws ParseException {
        return nnf.toNnf(input);
    }

    @Test
    @DisplayName("Atoms remain unchanged")
    void atomsUnchanged() throws Exception {
        assertEquals("a", nnfOf("a"));
        assertEquals("b", nnfOf("b"));
        assertEquals("c", nnfOf("c"));
    }

    @Test
    @DisplayName("Implication eliminated in NNF")
    void implicationEliminated() throws Exception {
        assertEquals("!a|b", nnfOf("a>b"));
        // reverse implication a<b becomes !b|a
        assertEquals("!b|a", nnfOf("a<b"));
    }

    @Test
    @DisplayName("Equivalence expanded to disjunction of conjunctions")
    void equivalenceExpanded() throws Exception {
        assertEquals("(a&b)|(!a&!b)", nnfOf("a=b"));
    }

    @Test
    @DisplayName("Negated Until becomes Release of negated operands")
    void notUntil() throws Exception {
        assertEquals("!aR!b", nnfOf("!(aUb)"));
    }

    @Test
    @DisplayName("Negated Release becomes Until of negated operands")
    void notRelease() throws Exception {
        assertEquals("!aU!b", nnfOf("!(aRb)"));
    }

    @Test
    @DisplayName("Negated Globally -> Finally of negated inner")
    void notGlobally() throws Exception {
        assertEquals("F!a", nnfOf("!Ga"));
    }

    @Test
    @DisplayName("Negated Finally -> Globally of negated inner")
    void notFinally() throws Exception {
        assertEquals("G!a", nnfOf("!Fa"));
    }

    @Test
    @DisplayName("Negated Next/WeakNext push inside")
    void notNextVariants() throws Exception {
        assertEquals("X!a", nnfOf("!Xa"));
        assertEquals("W!a", nnfOf("!Wa"));
    }

    @Test
    @DisplayName("De Morgan for And/Or under negation")
    void deMorgan() throws Exception {
        assertEquals("!a&!b", nnfOf("!(a|b)"));
        assertEquals("!a|!b", nnfOf("!(a&b)"));
    }

    @Test
    @DisplayName("Double negation removed")
    void doubleNegation() throws Exception {
        assertEquals("a", nnfOf("!!a"));
    }

    @Test
    @DisplayName("No implication/equivalence symbols remain after NNF")
    void noImplSymbolsRemain() throws Exception {
        String[] inputs = {"a>b", "a<b", "a=b", "(a=b)>(a<b)", "(a>b)=(a=b)"};
        for (String in : inputs) {
            String out = nnfOf(in);
            assertFalse(out.contains(">"), () -> "NNF should not contain '>' for " + in + " -> " + out);
            assertFalse(out.contains("<"), () -> "NNF should not contain '<' for " + in + " -> " + out);
            assertFalse(out.contains("="), () -> "NNF should not contain '=' for " + in + " -> " + out);
        }
    }
}
