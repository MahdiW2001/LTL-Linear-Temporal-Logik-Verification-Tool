package com.ltl.ltl.service.tableau;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.ltl.ltl.model.AtomicProposition;
import com.ltl.ltl.model.IFormula;
import com.ltl.ltl.model.Symbol;
import com.ltl.ltl.model.formulas.And;
import com.ltl.ltl.model.formulas.Globally;
import com.ltl.ltl.model.formulas.Next;
import com.ltl.ltl.model.formulas.Not;
import com.ltl.ltl.model.formulas.Or;
import com.ltl.ltl.model.formulas.Release;
import com.ltl.ltl.model.formulas.Until;
import com.ltl.ltl.model.tableau.TableauEntry;
import com.ltl.ltl.model.tableau.TableauNode;

class TableauSatTest {

    private TableauNode nodeFrom(IFormula... fs) {
        ArrayList<TableauEntry> entries = new ArrayList<>();
        for (IFormula f : fs) {
            entries.add(new TableauEntry(f));
        }
        return new TableauNode(entries);
    }

    @Test
    void sat_true_onSingleAtomic() {
        TableauNode root = nodeFrom(new AtomicProposition(Symbol.A));
        boolean sat = new TableauProof(root).sat(root);
        assertTrue(sat, "Single atomic proposition should be satisfiable");
    }

    @Test
    void sat_false_onContradictoryAtomAndNegation() {
        TableauNode root = nodeFrom(
            new AtomicProposition(Symbol.A),
            new Not(new AtomicProposition(Symbol.A))
        );
        boolean sat = new TableauProof(root).sat(root);
        assertFalse(sat, "A and ¬A in the same node should be unsatisfiable");
    }

    @Test
    void sat_true_onPropositionalTautologyOr() {
        TableauNode root = nodeFrom(new Or(
            new AtomicProposition(Symbol.A),
            new Not(new AtomicProposition(Symbol.A))
        ));
        boolean sat = new TableauProof(root).sat(root);
        assertTrue(sat, "A ∨ ¬A should be satisfiable");
    }

    @Test
    void sat_false_orBlockedByNegations() {
        // (A ∨ B) with ¬A and ¬B simultaneously should force both branches to fail
        TableauNode root = nodeFrom(
            new Or(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.B)),
            new Not(new AtomicProposition(Symbol.A)),
            new Not(new AtomicProposition(Symbol.B))
        );
        boolean sat = new TableauProof(root).sat(root);
        assertFalse(sat, "(A ∨ B) with ¬A and ¬B should be unsatisfiable");
    }

    @Test
    void sat_false_onGloballyA_and_GloballyNotA() {
        // G A ∧ G ¬A must immediately lead to A and ¬A at the same step
        TableauNode root = nodeFrom(
            new Globally(new AtomicProposition(Symbol.A)),
            new Globally(new Not(new AtomicProposition(Symbol.A)))
        );
        boolean sat = new TableauProof(root).sat(root);
        assertFalse(sat, "G A ∧ G ¬A should be unsatisfiable");
    }

    @Test
    void sat_true_onOrInsideGloballyWithSupport() {
        // G(A ∨ B) ∧ ¬A is satisfiable by choosing B branch each step
        TableauNode root = nodeFrom(
            new Globally(new Or(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.B))),
            new Not(new AtomicProposition(Symbol.A))
        );
        boolean sat = new TableauProof(root).sat(root);
        assertTrue(sat, "G(A ∨ B) with ¬A should be satisfiable (choose B)");
    }

    @Test
    void sat_false_onAndWithImmediateContradiction() {
        // (A ∧ ¬A) should be unsatisfiable at the same node
        TableauNode root = nodeFrom(new And(
            new AtomicProposition(Symbol.A),
            new Not(new AtomicProposition(Symbol.A))
        ));
        boolean sat = new TableauProof(root).sat(root);
        assertFalse(sat, "(A ∧ ¬A) should be unsatisfiable");
    }

    @Test
    void sat_true_complexOrMix() {
        // (A ∨ B) ∧ (¬A ∨ B) ∧ (A ∨ ¬B) is satisfiable (e.g., pick A=true, B=true)
        TableauNode root = nodeFrom(
            new And(
                new Or(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.B)),
                new Or(new Not(new AtomicProposition(Symbol.A)), new AtomicProposition(Symbol.B))
            ),
            new Or(new AtomicProposition(Symbol.A), new Not(new AtomicProposition(Symbol.B)))
        );
        boolean sat = new TableauProof(root).sat(root);
        assertTrue(sat, "Complex OR mix should be satisfiable");
    }

    @Test
    void sat_true_globallyOr_and_until() {
        // G(A ∨ B) ∧ (A U C) is satisfiable (make C true eventually; until then A can hold)
        TableauNode root = nodeFrom(
            new Globally(new Or(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.B))),
            new Until(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.C))
        );
        boolean sat = new TableauProof(root).sat(root);
        assertTrue(sat, "G(A ∨ B) ∧ (A U C) should be satisfiable");
    }

    @Test
    void sat_false_globallyA_and_untilEventuallyNotA() {
        // G(A) ∧ (B U ¬A) is unsatisfiable because ¬A must eventually hold
        TableauNode root = nodeFrom(
            new Globally(new AtomicProposition(Symbol.A)),
            new Until(new AtomicProposition(Symbol.B), new Not(new AtomicProposition(Symbol.A)))
        );
        boolean sat = new TableauProof(root).sat(root);
        assertFalse(sat, "G(A) ∧ (B U ¬A) should be unsatisfiable");
    }

    @Test
    void sat_true_release_with_right_now_and_left_optional() {
        // (A R B) ∧ ¬A ∧ B is satisfiable (Release requires B now; A is optional if B holds)
        TableauNode root = nodeFrom(
            new Release(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.B)),
            new Not(new AtomicProposition(Symbol.A)),
            new AtomicProposition(Symbol.B)
        );
        boolean sat = new TableauProof(root).sat(root);
        assertTrue(sat, "(A R B) ∧ ¬A ∧ B should be satisfiable");
    }

    @Test
    void sat_false_release_violating_right_now() {
        // (A R B) ∧ ¬B is unsatisfiable: Release requires B to hold at all times, including now
        TableauNode root = nodeFrom(
            new Release(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.B)),
            new Not(new AtomicProposition(Symbol.B))
        );
        boolean sat = new TableauProof(root).sat(root);
        assertFalse(sat, "(A R B) ∧ ¬B should be unsatisfiable");
    }

    @Test
    void sat_true_next_progress_over_steps() {
        // X A ∧ ¬A: current step has ¬A; next step requires A → satisfiable via transition
        TableauNode root = nodeFrom(
            new Next(new AtomicProposition(Symbol.A)),
            new Not(new AtomicProposition(Symbol.A))
        );
        boolean sat = new TableauProof(root).sat(root);
        assertTrue(sat, "X A ∧ ¬A should be satisfiable (progress to next state)");
    }

    @Test
    void sat_true_double_next_progress() {
        // X X A ∧ ¬A ∧ X ¬A: progresses over two steps to satisfy A, consistent along the way
        TableauNode root = nodeFrom(
            new Next(new Next(new AtomicProposition(Symbol.A))),
            new Not(new AtomicProposition(Symbol.A)),
            new Next(new Not(new AtomicProposition(Symbol.A)))
        );
        boolean sat = new TableauProof(root).sat(root);
        assertTrue(sat, "Double next progression should be satisfiable");
    }

    @Test
    void sat_false_loop_reoccurs_without_fulfilling_next_finally() {
        // G(¬B) ∧ X(F B): eventuality X(F B) can never be fulfilled because ¬B holds globally
        TableauNode root = nodeFrom(
            new Globally(new Not(new AtomicProposition(Symbol.B))),
            new Next(new com.ltl.ltl.model.formulas.Finally(new AtomicProposition(Symbol.B)))
        );
        boolean sat = new TableauProof(root).sat(root);
        assertFalse(sat, "G(¬B) ∧ X(F B) should be unsatisfiable due to unfulfilled eventuality");
    }

    @Test
    void sat_true_until_and_release_meet_on_B_now() {
        // (A U B) ∧ (A R B) is satisfiable by making B hold now
        TableauNode root = nodeFrom(
            new Until(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.B)),
            new Release(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.B))
        );
        boolean sat = new TableauProof(root).sat(root);
        assertTrue(sat, "(A U B) ∧ (A R B) should be satisfiable");
    }

    @Test
    void sat_true_globally_or_with_until_to_A() {
        // G(A ∨ B) ∧ (B U A): satisfiable, since A can hold now or eventually, B until then
        TableauNode root = nodeFrom(
            new Globally(new Or(new AtomicProposition(Symbol.A), new AtomicProposition(Symbol.B))),
            new Until(new AtomicProposition(Symbol.B), new AtomicProposition(Symbol.A))
        );
        boolean sat = new TableauProof(root).sat(root);
        assertTrue(sat, "G(A ∨ B) ∧ (B U A) should be satisfiable");
    }

    @Test
    void sat_false_contradictory_global_constraints() {
        // G(A) ∧ G(¬A) is unsat (already covered via immediate clash after expansion)
        TableauNode root = nodeFrom(
            new Globally(new AtomicProposition(Symbol.A)),
            new Globally(new Not(new AtomicProposition(Symbol.A)))
        );
        boolean sat = new TableauProof(root).sat(root);
        assertFalse(sat, "G(A) ∧ G(¬A) should be unsatisfiable");
    }
}
