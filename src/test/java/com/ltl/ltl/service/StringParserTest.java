package com.ltl.ltl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.ltl.ltl.model.AtomicProposition;
import com.ltl.ltl.model.IFormula;
import com.ltl.ltl.model.Symbol;
import com.ltl.ltl.model.formulas.And;
import com.ltl.ltl.model.formulas.Equiv;
import com.ltl.ltl.model.formulas.Finally;
import com.ltl.ltl.model.formulas.Globally;
import com.ltl.ltl.model.formulas.Impl;
import com.ltl.ltl.model.formulas.Next;
import com.ltl.ltl.model.formulas.Not;
import com.ltl.ltl.model.formulas.Or;
import com.ltl.ltl.model.formulas.RImpl;
import com.ltl.ltl.model.formulas.Release;
import com.ltl.ltl.model.formulas.Until;
import com.ltl.ltl.model.formulas.WeakNext;

public class StringParserTest {

    private final StringParser sp = new StringParser();

    private String roundTrip(String input) throws Exception {
        AstParser parser = new AstParser(new com.ltl.ltl.service.syntax.Lexer(input));
        IFormula ast = parser.parseFormula();
        return sp.toString(ast);
    }

    @Test
    void atomicPropositionsToString() {
        assertEquals("a", sp.toString(new AtomicProposition(Symbol.A)));
        assertEquals("b", sp.toString(new AtomicProposition(Symbol.B)));
        assertEquals("c", sp.toString(new AtomicProposition(Symbol.C)));
    }

    @Test
    void unaryOperatorsToString() {
        assertEquals("Fa", sp.toString(new Finally(new AtomicProposition(Symbol.A))));
        assertEquals("Gb", sp.toString(new Globally(new AtomicProposition(Symbol.B))));
        assertEquals("Xc", sp.toString(new Next(new AtomicProposition(Symbol.C))));
        assertEquals("Wa", sp.toString(new WeakNext(new AtomicProposition(Symbol.A))));
    }

    @Test
    void untilAndReleaseToString() {
        assertEquals("aUb", sp.toString(new Until(
                new AtomicProposition(Symbol.A),
                new AtomicProposition(Symbol.B))));
        assertEquals("bRc", sp.toString(new Release(
                new AtomicProposition(Symbol.B),
                new AtomicProposition(Symbol.C))));
    }

    @Test
    void booleanAndImplicationToString() {
        assertEquals("a&b", sp.toString(new And(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.B))));
        assertEquals("a|b", sp.toString(new Or(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.B))));
        assertEquals("a>b", sp.toString(new Impl(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.B))));
        assertEquals("a<b", sp.toString(new RImpl(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.B))));
        assertEquals("a=b", sp.toString(new Equiv(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.B))));
    }

    @Test
    void nestedFormulasToString() {
        IFormula nested = new Globally(
                new Until(
                        new AtomicProposition(Symbol.A),
                        new Finally(new AtomicProposition(Symbol.B))
                )
        );
        assertEquals("G(aU(Fb))", sp.toString(nested));
    }

    @Test
    void deepNestingMix() {
        IFormula f = new Finally(
                new Globally(
                        new Until(
                                new AtomicProposition(Symbol.A),
                                new Release(
                                        new AtomicProposition(Symbol.B),
                                        new Next(new AtomicProposition(Symbol.C))
                                )
                        )
                )
        );
        assertEquals("F(G(aU(bR(Xc))))", sp.toString(f));
    }

    @Test
    void roundTripSimple() throws Exception {
        String input = "G(aUb)";
        assertEquals(input, roundTrip(input));
    }

    @Test
    void roundTripComplex1() throws Exception {
        String input = "F(G(aUb))";
        assertEquals(input, roundTrip(input));
    }

    @Test
    void roundTripComplex2() throws Exception {
        String input = "X(aRb)";
        assertEquals(input, roundTrip(input));
    }

    @Test
    void roundTripComplex3() throws Exception {
        String input = "W(G(aU(Fb)))";
        assertEquals(input, roundTrip(input));
    }

    @Test
    void roundTripLongChain() throws Exception {
        String input = "(G(aUb))U(Fc)";
        assertEquals(input, roundTrip(input));
    }

    @Test
    void roundTripWithNestedRelease() throws Exception {
        String input = "(F(aRb))R(Gc)";
        assertEquals(input, roundTrip(input));
    }

    @Test
    void roundTripDoubleUnary() throws Exception {
        String input = "G(Ga)";
        assertEquals(input, roundTrip(input));
    }

    @Test
    void roundTripUnaryInsideBinary() throws Exception {
        String input = "aU(Gb)";
        assertEquals(input, roundTrip(input));
    }

    @Test
    void roundTripDeeplyNested() throws Exception {
        String input = "G(F(aUb))";
        assertEquals(input, roundTrip(input));
    }

    @Test
    void roundTripWeakNext() throws Exception {
        String input = "Wa";
        assertEquals(input, roundTrip(input));
    }

    @Test
    void roundTripBigMix() throws Exception {
        String input = "(G(aU(Fb)))R(Xc)";
        assertEquals(input, roundTrip(input));
    }

    @Test
    void roundTripBooleanAndImplication() throws Exception {
        String input = "(a&b)|(a>b)";
        assertEquals(input, roundTrip(input));
        input = "(a<b)=(a=b)";
        assertEquals(input, roundTrip(input));
    }

     @Test
    void notOnAtomicToString() {
        IFormula notA = new Not(new AtomicProposition(Symbol.A));
        assertEquals("!a", sp.toString(notA));
    }

    @Test
    void notOnNestedFormulaToString() {
        IFormula f = new Not(
                new Globally(
                        new Until(
                                new AtomicProposition(Symbol.A),
                                new Finally(new AtomicProposition(Symbol.B))
                        )
                )
        );
        assertEquals("!(G(aU(Fb)))", sp.toString(f));
    }

    @Test
    void roundTripNotSimple() throws Exception {
        String input = "!a";
        assertEquals(input, roundTrip(input));
    }

    @Test
    void roundTripNotComplex() throws Exception {
        String input = "!(G(aUb))";
        assertEquals(input, roundTrip(input));
    }

    @Test
    void doubleNegationRoundTrip() throws Exception {
        String input = "!(!a)";
        assertEquals(input, roundTrip(input));
    }
}
