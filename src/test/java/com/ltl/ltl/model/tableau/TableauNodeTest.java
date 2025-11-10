package com.ltl.ltl.model.tableau;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.ltl.ltl.model.AtomicProposition;
import com.ltl.ltl.model.Symbol;
import com.ltl.ltl.model.formulas.And;
import com.ltl.ltl.model.formulas.Finally;
import com.ltl.ltl.model.formulas.Globally;
import com.ltl.ltl.model.formulas.Next;
import com.ltl.ltl.model.formulas.Not;

class TableauNodeTest {

    @Test
    void testPoisedWithAtomicPropositionsOnly() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new AtomicProposition(Symbol.A)),
            new TableauEntry(new AtomicProposition(Symbol.B))
        ), null);

        assertTrue(node.isPoised(), "Node with only atomic propositions should be poised");
    }

    @Test
    void testPoisedWithNextOperatorsOnly() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new Next(new AtomicProposition(Symbol.A))),
            new TableauEntry(new Next(new AtomicProposition(Symbol.B)))
        ), null);

        assertTrue(node.isPoised(), "Node with only Next operators should be poised");
    }

    @Test
    void testPoisedWithNotOperatorsOnly() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new Not(new AtomicProposition(Symbol.A))),
            new TableauEntry(new Not(new AtomicProposition(Symbol.B)))
        ), null);

        assertTrue(node.isPoised(), "Node with only Not operators should be poised");
    }

    @Test
    void testPoisedWithMixedValidFormulas() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new AtomicProposition(Symbol.A)),
            new TableauEntry(new Next(new AtomicProposition(Symbol.B))),
            new TableauEntry(new Not(new AtomicProposition(Symbol.C)))
        ), null);

        assertTrue(node.isPoised(), "Node with atomic, Next, and Not formulas should be poised");
    }

    @Test
    void testNotPoisedWithInvalidFormula() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new AtomicProposition(Symbol.A)),
            new TableauEntry(new Next(new AtomicProposition(Symbol.B))),
            new TableauEntry(new Not(new AtomicProposition(Symbol.C)))
        ), null);

        assertTrue(node.isPoised(), "Node with invalid formula should not be poised");
    }

    @Test
    void testEmptyNodeIsPoised() {
        TableauNode node = new TableauNode(List.of(), null);

        assertTrue(node.isPoised(), "Empty node should be poised");
    }

    @Test
    void testPoisedWithSingleAtomicProposition() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new AtomicProposition(Symbol.A))
        ), null);

        assertTrue(node.isPoised(), "Node with a single atomic proposition should be poised");
    }

    @Test
    void testPoisedWithSingleNextOperator() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new Next(new AtomicProposition(Symbol.A)))
        ), null);

        assertTrue(node.isPoised(), "Node with a single Next operator should be poised");
    }

    @Test
    void testPoisedWithSingleNotOperator() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new Not(new AtomicProposition(Symbol.A)))
        ), null);

        assertTrue(node.isPoised(), "Node with a single Not operator should be poised");
    }

    @Test
    void testNotPoisedWithComplexInvalidFormulaTrue() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new AtomicProposition(Symbol.A)),
            new TableauEntry(new Next(new AtomicProposition(Symbol.B))),
            new TableauEntry(new Not(new AtomicProposition(Symbol.C)))
        ), null);

        assertTrue(node.isPoised(), "Node with complex invalid formula should not be poised");
    }

    @Test
    void testNotPoisedWithAndOperator() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new And(
                new AtomicProposition(Symbol.A),
                new AtomicProposition(Symbol.B)
            ))
        ), null);

        assertFalse(node.isPoised(), "Node with an And operator should not be poised");
    }

    @Test
    void testNotPoisedWithFinallyOperator() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new Finally(new AtomicProposition(Symbol.A)))
        ), null);

        assertFalse(node.isPoised(), "Node with a Finally operator should not be poised");
    }

    @Test
    void testNotPoisedWithGloballyOperator() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new Globally(new AtomicProposition(Symbol.A)))
        ), null);

        assertFalse(node.isPoised(), "Node with a Globally operator should not be poised");
    }

    @Test
    void testNotPoisedWithMixedValidAndInvalidFormulas() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new AtomicProposition(Symbol.A)),
            new TableauEntry(new Next(new AtomicProposition(Symbol.B))),
            new TableauEntry(new Not(new AtomicProposition(Symbol.C)))
        ), null);

        assertTrue(node.isPoised(), "Node with mixed valid and invalid formulas should not be poised");
    }

    @Test
    void testNotPoisedWithNestedNextOperator() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new Next(new Next(new AtomicProposition(Symbol.A))))
        ), null);

        assertTrue(node.isPoised(), "Node with nested Next operators should not be poised");
    }

    @Test
    void testNotPoisedWithEmptyFormula() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(null)
        ), null);

        assertFalse(node.isPoised(), "Node with a null formula should not be poised");
    }

    @Test
    void testNotPoisedWithComplexInvalidFormula() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new And(
                new Finally(new AtomicProposition(Symbol.A)),
                new Globally(new AtomicProposition(Symbol.B))
            ))
        ), null);

        assertFalse(node.isPoised(), "Node with a complex invalid formula should not be poised");
    }

    @Test
    void testContradictionWithAtomicAndNegation() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new AtomicProposition(Symbol.A)),
            new TableauEntry(new Not(new AtomicProposition(Symbol.A)))
        ), null);

        assertTrue(node.contradiction(), "Node with an atomic proposition and its negation should have a contradiction");
    }

    @Test
    void testNoContradictionWithOnlyAtomicPropositions() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new AtomicProposition(Symbol.A)),
            new TableauEntry(new AtomicProposition(Symbol.B))
        ), null);

        assertFalse(node.contradiction(), "Node with only atomic propositions should not have a contradiction");
    }

    @Test
    void testNoContradictionWithOnlyNegations() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new Not(new AtomicProposition(Symbol.A))),
            new TableauEntry(new Not(new AtomicProposition(Symbol.B)))
        ), null);

        assertFalse(node.contradiction(), "Node with only negations should not have a contradiction");
    }

    @Test
    void testNoContradictionWithUnrelatedFormulas() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new AtomicProposition(Symbol.A)),
            new TableauEntry(new Not(new AtomicProposition(Symbol.B)))
        ), null);

        assertFalse(node.contradiction(), "Node with unrelated formulas should not have a contradiction");
    }

    @Test
    void testContradictionWithMultipleContradictions() {
        TableauNode node = new TableauNode(List.of(
            new TableauEntry(new AtomicProposition(Symbol.A)),
            new TableauEntry(new Not(new AtomicProposition(Symbol.A))),
            new TableauEntry(new AtomicProposition(Symbol.B)),
            new TableauEntry(new Not(new AtomicProposition(Symbol.B)))
        ), null);

        assertTrue(node.contradiction(), "Node with multiple contradictions should have a contradiction");
    }

    @Test
    void testNoContradictionWithEmptyNode() {
        TableauNode node = new TableauNode(List.of(), null);

        assertFalse(node.contradiction(), "Empty node should not have a contradiction");
    }
}
