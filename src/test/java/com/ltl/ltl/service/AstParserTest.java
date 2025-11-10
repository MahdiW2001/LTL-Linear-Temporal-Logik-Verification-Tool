package com.ltl.ltl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.ltl.ltl.model.formulas.Or;
import com.ltl.ltl.model.formulas.RImpl;
import com.ltl.ltl.model.formulas.Release;
import com.ltl.ltl.model.formulas.Until;
import com.ltl.ltl.model.formulas.WeakNext;
import com.ltl.ltl.service.syntax.Lexer;
import com.ltl.ltl.service.syntax.ParseException;

public class AstParserTest {

    private IFormula parse(String input) throws ParseException {
        AstParser parser = new AstParser(new Lexer(input));
        return parser.parseFormula();
    }

    @Test
    void parsesAtomicPropositions() throws ParseException {
        IFormula f = parse("a");
        assertTrue(f instanceof AtomicProposition);
        assertEquals(Symbol.A, ((AtomicProposition) f).getSymbol());

        f = parse("b");
        assertTrue(f instanceof AtomicProposition);
        assertEquals(Symbol.B, ((AtomicProposition) f).getSymbol());
    }

    @Test
    void parsesUnaryOperators() throws ParseException {
        IFormula f = parse("Ga");
        assertTrue(f instanceof Globally);
    assertTrue(((Globally) f).getFormula() instanceof AtomicProposition);

        f = parse("Xa");
        assertTrue(f instanceof Next);
    assertTrue(((Next) f).getFormula() instanceof AtomicProposition);

        f = parse("Fa");
        assertTrue(f instanceof Finally);
    assertTrue(((Finally) f).getFormula() instanceof AtomicProposition);

        f = parse("Wa");
        assertTrue(f instanceof WeakNext);
    assertTrue(((WeakNext) f).getFormula() instanceof AtomicProposition);
    }

    @Test
    void parsesUntilAndRelease() throws ParseException {
        IFormula f = parse("aUb");
        assertTrue(f instanceof Until);
        Until u = (Until) f;
        assertTrue(u.getLeft() instanceof AtomicProposition);
        assertTrue(u.getRight() instanceof AtomicProposition);

        f = parse("aRb");
        assertTrue(f instanceof Release);
        Release r = (Release) f;
        assertTrue(r.getLeft() instanceof AtomicProposition);
        assertTrue(r.getRight() instanceof AtomicProposition);
    }

    @Test
    void parsesBooleanAndImplicationOperators() throws ParseException {
        IFormula f = parse("a&b");
        assertTrue(f instanceof And);
        f = parse("a|b");
        assertTrue(f instanceof Or);
        f = parse("a>b");
        assertTrue(f instanceof Impl);
        f = parse("a<b");
        assertTrue(f instanceof RImpl);
        f = parse("a=b");
        assertTrue(f instanceof Equiv);
    }

    @Test
    void andOrPrecedence() throws ParseException {
        // a&b|c -> (a&b)|c
        IFormula f = parse("a&b|c");
        assertTrue(f instanceof Or);
        Or or = (Or) f;
        assertTrue(or.getLeft() instanceof And); // left operand should be And
        assertTrue(or.getRight() instanceof AtomicProposition);
    }

    @Test
    void implicationInsideEquivPrecedence() throws ParseException {
        // a=b>c -> a=(b>c)
        IFormula f = parse("a=b>c");
        assertTrue(f instanceof Equiv);
        Equiv eq = (Equiv) f;
        assertTrue(eq.getLeft() instanceof AtomicProposition);
        assertTrue(eq.getRight() instanceof Impl);

        // a>b=c -> (a>b)=c
        f = parse("a>b=c");
        assertTrue(f instanceof Equiv);
        eq = (Equiv) f;
        assertTrue(eq.getLeft() instanceof Impl);
        assertTrue(eq.getRight() instanceof AtomicProposition);
    }

    @Test
    void equivalenceIsLeftAssociative() throws ParseException {
        // a=b=c -> (a=b)=c with current grammar (left associative)
        IFormula f = parse("a=b=c");
        assertTrue(f instanceof Equiv);
        Equiv outer = (Equiv) f;
        assertTrue(outer.getLeft() instanceof Equiv);
        assertTrue(outer.getRight() instanceof AtomicProposition);
    }

    @Test
    void parsesNestedParentheses() throws ParseException {
        IFormula f = parse("(G(a))U(F(b))");
        assertTrue(f instanceof Until);

        Until u = (Until) f;
        assertTrue(u.getLeft() instanceof Globally);
        assertTrue(u.getRight() instanceof Finally);
    }

    @Test
    void invalidFormulaThrows() {
        assertThrows(ParseException.class, () -> parse("G"));   // fehlt Operand
        assertThrows(ParseException.class, () -> parse("(a"));  // Klammer offen
        assertThrows(ParseException.class, () -> parse("a&&b")); // ungültig
    }
}
