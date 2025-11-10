package com.ltl.ltl.service.tableau;
import com.ltl.ltl.model.IFormula;
import com.ltl.ltl.model.formulas.And;
import com.ltl.ltl.model.formulas.Finally;
import com.ltl.ltl.model.formulas.Globally;
import com.ltl.ltl.model.formulas.Next;
import com.ltl.ltl.model.formulas.Or;
import com.ltl.ltl.model.formulas.Release;
import com.ltl.ltl.model.formulas.Until;
import com.ltl.ltl.model.tableau.TableauEntry;
import com.ltl.ltl.model.tableau.TableauNode;
public class TableauTreeConstruction {

    private TableauNode root;

    

    public TableauTreeConstruction(TableauNode root) {
        this.root = root;
    }

    public TableauNode build() {
        buildRec(root);
        return root;
    }

    private void buildRec(TableauNode node) {
        if (node == null) return;

        // Stop if contradiction at node
        if (node.contradiction()) {
            node.setIsClosed(false);
            return;
        }

        // If node is poised and has no Next, it's a satisfying leaf for this step
        if (node.isPoised() && !node.containsNextOperator()) {
            node.setIsClosed(true);
            return;
        }

        // If poised and has nexts, handle loop and next-step
        if (node.isPoised() && node.containsNextOperator()) {
            if (TableauProof.hasAncestorWithSameEntriesAndFulfilledNextEventualities(node)) {
                node.setIsClosed(true);
                return;
            }
            if (TableauProof.checkOccurrenceInPath(node)) {
                // Loop without fulfilled eventualities: non-closing (rejecting)
                node.setIsClosed(false);
                return;
            }
            TableauNode next = TableauProof.nextStepNode(node);
            if (next == null) {
                // Non-closing: cannot advance to next state
                node.setIsClosed(false);
                return;
            }
            // nextStepNode already links child; recurse and mark parent as undecided
            buildRec(next);
            node.setIsClosed(null);
            return;
        }

        // Expand first unchecked formula similar to TableauProof.sat
        for (TableauEntry entry : node.getEntries()) {
            IFormula f = entry.getFormula();
            if (f == null || entry.isChecked()) continue;

            if ((f instanceof Globally || f instanceof And || f instanceof Next) && !entry.isChecked()) {
                TableauNode expanded = TableauProof.expandNodeNoBranching(node, entry);
                // expandNodeNoBranching already links the child; recurse and mark parent as undecided
                buildRec(expanded);
                node.setIsClosed(null);
                return;
            }
            if ((f instanceof Or || f instanceof Until || f instanceof Release || f instanceof Finally) && !entry.isChecked()) {
                TableauNode left = TableauProof.expandNodeLeftBranching(node, entry);
                TableauNode right = TableauProof.expandNodeRightBranching(node, entry);
                // Recurse into both children to fully materialize the tableau rule
                buildRec(left);
                buildRec(right);
                // Parent node has children: keep parent as undecided for visualization
                node.setIsClosed(null);
                return;
            }
        }

        // No more successors from this node; true if accepting, false if dead-end (non-closing), else null only for ambiguous branch aggregation
        Boolean accepted = node.getIsClosed();
        if (accepted == null) {
            if (TableauProof.hasAncestorWithSameEntriesAndFulfilledNextEventualities(node)) {
                node.setIsClosed(true);
            } else {
                node.setIsClosed(false);
            }
        }
    }

}
