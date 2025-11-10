package com.ltl.ltl.service.tableau;

import java.util.ArrayList;
import java.util.List;

import com.ltl.ltl.model.AtomicProposition;
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

public class TableauProof {
    private TableauNode root;

    public TableauProof(TableauNode root) {
        this.root = root;
    }

    public TableauNode getRoot() {
        return root;
    }

    //sat solver via tableau 
    public boolean sat(TableauNode node) {
        if(node.contradiction()) {
            return false;}
        //check if node contains an expandable formula 
//         try {
//     Thread.sleep(2000); // Pause for 100 milliseconds
// } catch (InterruptedException e) {
//     Thread.currentThread().interrupt(); // Restore the interrupted status
//     System.err.println("Sleep was interrupted: " + e.getMessage());
// }
    System.out.println("Checking node: " + node.getEntries());
    for(TableauEntry entry : node.getEntries()) {
        System.out.println("Checking node: " + entry.getFormula());
    }
        
        for(TableauEntry entry : node.getEntries()) {
            if(entry.getFormula() instanceof Globally && !entry.isChecked()) {
                TableauNode expandedNode = expandNodeNoBranching(node, entry);
                 return sat(expandedNode);
            }
            if(entry.getFormula() instanceof And && !entry.isChecked()) {
                TableauNode expandedNode = expandNodeNoBranching(node, entry);
                return sat(expandedNode);
            }
            if(entry.getFormula() instanceof Or && !entry.isChecked()) {
                TableauNode leftBranch = expandNodeLeftBranching(node, entry);
                TableauNode rightBranch = expandNodeRightBranching(node, entry);
                return sat(leftBranch) || sat(rightBranch);
            }
            if(entry.getFormula() instanceof Until && !entry.isChecked()) {
                TableauNode leftBranch = expandNodeLeftBranching(node, entry);
                TableauNode rightBranch = expandNodeRightBranching(node, entry);
                return sat(leftBranch) || sat(rightBranch);
            }
            if(entry.getFormula() instanceof Release && !entry.isChecked()) {
                TableauNode leftBranch = expandNodeLeftBranching(node, entry);
                TableauNode rightBranch = expandNodeRightBranching(node, entry);
                return sat(leftBranch) || sat(rightBranch);
            }
            if(entry.getFormula() instanceof Next && !entry.isChecked()) {
                TableauNode expandedNode = expandNodeNoBranching(node, entry);
                return sat(expandedNode);
            }
        }

        // Poised step/loop handling
        if (node.isPoised() && node.containsNextOperator()) {
            // If there is a reoccurring state and next-eventualities are fulfilled → success
            if (hasAncestorWithSameEntriesAndFulfilledNextEventualities(node)) {
                return true;
            }
            // If there is a reoccurring state but next-eventualities are NOT fulfilled → fail
            if (checkOccurrenceInPath(node)) {
                return false;
            }
            // Otherwise, take the transition to the next state
            TableauNode nextNode = nextStepNode(node);
            if (nextNode != null) {
                return sat(nextNode);
            }
        }

        return true;
    }


    public static TableauNode expandNodeNoBranching(TableauNode node, TableauEntry entry) {
        List<TableauEntry> newEntries = new ArrayList<>(node.getEntries());

        // Mark the current entry as checked
        for (TableauEntry e : node.getEntries()) {
            if (e.getFormula().equals(entry.getFormula())) {
                e.markChecked();
            }
        }

    // Handle non-branching operators
        if (entry.getFormula() instanceof Globally globallyFormula) {
            // Globally φ -> Add φ and X(G φ)
            newEntries.add(new TableauEntry(globallyFormula.getFormula()));
            newEntries.add(new TableauEntry(new Next(entry.getFormula())));
        }

    // Handle non-branching operators
        if (entry.getFormula() instanceof And andFormula) {
            // Globally φ -> Add φ and X(G φ)
            newEntries.add(new TableauEntry(andFormula.getLeft()));
            newEntries.add(new TableauEntry(andFormula.getRight()));
        }
    // Deduplicate entries by formula signature and prefer checked=true when merging
    newEntries = mergeDedup(newEntries);
    // Create and return the new node
    TableauNode newNode = new TableauNode(newEntries);
        node.getChildren().add(newNode);
        newNode.setParent(node);
        return newNode;
    }
    
 public static TableauNode expandNodeLeftBranching(TableauNode node, TableauEntry entry) {
        // Work on a fresh list to avoid mutating the original node list
        List<TableauEntry> newEntries = new ArrayList<>(node.getEntries() );

        // Mark the expanded entry as checked in the current node
        for (TableauEntry e : node.getEntries()) {
            if (e.getFormula().equals(entry.getFormula())) {
                e.markChecked();
                break;
            }
        }

        IFormula f = entry.getFormula();

        // Or: left branch takes the left disjunct
        if (f instanceof Or orFormula) {
            newEntries.add(new TableauEntry(orFormula.getLeft()));
        }
        // Finally: left branch takes the inner now
        else if (f instanceof Finally fin) {
            newEntries.add(new TableauEntry(fin.getFormula()));
        }
        // Until: "right holds now" branch
        else if (f instanceof Until untilFormula) {
            newEntries.add(new TableauEntry(untilFormula.getRight()));
        }
        // Release: "both hold now" branch
        else if (f instanceof Release releaseFormula) {
            newEntries.add(new TableauEntry(new And(
                releaseFormula.getLeft(),
                releaseFormula.getRight()
            )));
        } 
    // Deduplicate entries by formula signature and prefer checked=true when merging
    newEntries = mergeDedup(newEntries);
    TableauNode newNode = new TableauNode(newEntries);
        node.getChildren().add(newNode);
        newNode.setParent(node);
        return newNode;
    }

    public static TableauNode expandNodeRightBranching(TableauNode node, TableauEntry entry) {
        // Work on a fresh list to avoid mutating the original node list
        java.util.List<TableauEntry> newEntries = new java.util.ArrayList<>(node.getEntries());

        // Mark the expanded entry as checked in the current node
        for (TableauEntry e : node.getEntries()) {
            if (e.getFormula().equals(entry.getFormula())) {
                e.markChecked();
                break;
            }
        }

        IFormula f = entry.getFormula();

        // Or: right branch takes the right disjunct
        if (f instanceof com.ltl.ltl.model.formulas.Or orFormula) {
            newEntries.add(new TableauEntry(orFormula.getRight()));
        }
        // Finally: right branch postpones, X(F φ)
    else if (f instanceof com.ltl.ltl.model.formulas.Finally) {
            newEntries.add(new TableauEntry(new com.ltl.ltl.model.formulas.Next(f)));
        }
        // Until: "left holds and postpone" branch
        else if (f instanceof com.ltl.ltl.model.formulas.Until untilFormula) {
            newEntries.add(new TableauEntry(new com.ltl.ltl.model.formulas.And(
                untilFormula.getLeft(),
                new com.ltl.ltl.model.formulas.Next(f) // X(φ U ψ)
            )));
        }
        // Release: "right holds and postpone" branch
        else if (f instanceof com.ltl.ltl.model.formulas.Release releaseFormula) {
            newEntries.add(new TableauEntry(new com.ltl.ltl.model.formulas.And(
                releaseFormula.getRight(),
                new com.ltl.ltl.model.formulas.Next(f) // X(φ R ψ)
            )));
        } 
    // Deduplicate entries by formula signature and prefer checked=true when merging
    newEntries = mergeDedup(newEntries);
    TableauNode newNode = new TableauNode(newEntries);
        node.getChildren().add(newNode);
        newNode.setParent(node);
        return newNode;
    }
public static boolean checkOccurrenceInPath(TableauNode node) {
        // Iterative walk up the parent chain to avoid deep recursion/StackOverflow
        if (node == null) return false;
        TableauNode cur = node.getParent();
        while (cur != null) {
            if (haveSameEntries(node, cur)) {
                return true;
            }
            cur = cur.getParent();
        }
        return false;
    }

// Multiset equality of entry formulas by string signature (order-insensitive)
private static boolean haveSameEntries(TableauNode a, TableauNode b) {
    java.util.Map<String, Integer> freq = new java.util.HashMap<>();
    for (TableauEntry e : a.getEntries()) {
        String s = sig(e.getFormula());
        // use Integer::sum (BiFunction) to increment
        freq.merge(s, 1, Integer::sum);
    }
    for (TableauEntry e : b.getEntries()) {
        String s = sig(e.getFormula());
        Integer c = freq.get(s);
        if (c == null) return false;
        if (c == 1) {
            freq.remove(s);
        } else {
            freq.put(s, c - 1);
        }
    }
    return freq.isEmpty();
}

private static String sig(IFormula f) {
    return (f == null) ? "<null>" : f.toString();
}

// Scan from 'node' up to and including 'ancestor' and ensure each target appears at least once
private static boolean targetsFulfilledOnPath(TableauNode ancestor, TableauNode node, java.util.Set<IFormula> targets) {
    java.util.Set<String> remaining = new java.util.HashSet<>();
    for (IFormula t : targets) remaining.add(sig(t));

    TableauNode cur = node;
    while (cur != null) {
        for (TableauEntry e : cur.getEntries()) {
            remaining.remove(sig(e.getFormula()));
            if (remaining.isEmpty()) return true;
        }
        if (cur == ancestor) break; // processed ancestor too
        cur = cur.getParent();      // advance exactly once
    }
    return remaining.isEmpty();
}

// Collect required witnesses for next-eventualities in a node:
// - X(F φ)  → require φ on the path
// - X(α U β) → require β on the path
private static java.util.Set<IFormula> collectNextEventualityTargets(TableauNode n) {
    java.util.Set<IFormula> targets = new java.util.HashSet<>();
    for (TableauEntry e : n.getEntries()) {
        IFormula f = e.getFormula();
        if (f instanceof Next nxt) {
            IFormula inner = nxt.getFormula();
            if (inner instanceof Finally fin) {
                targets.add(fin.getFormula());
            } else if (inner instanceof Until un) {
                targets.add(un.getRight());
            }
        }
    }
    return targets;
}

    public static TableauNode nextStepNode(TableauNode currentNode) {
        // Build next-state entries:
        // - drop AtomicProposition and Not(AtomicProposition)
        // - for Next(φ), add φ
        List<TableauEntry> newEntries = new ArrayList<>();

        for (TableauEntry entry : currentNode.getEntries()) {
            IFormula f = entry.getFormula();

            if (f instanceof Next n) {
                newEntries.add(new TableauEntry(n.getFormula()));
            } else if (f instanceof AtomicProposition) {
            } else if (f instanceof com.ltl.ltl.model.formulas.Not notF
                    && notF.getFormula() instanceof AtomicProposition) {
            }
        }

    // Deduplicate entries by formula signature and prefer checked=true when merging
    newEntries = mergeDedup(newEntries);
    TableauNode newNode = new TableauNode(newEntries);
        currentNode.getChildren().add(newNode);
        newNode.setParent(currentNode);
        return newNode;
    }

    /**
     * Returns true if there exists an ancestor of the given node that has the same entries,
     * and for every "next eventuality" present in that ancestor, the required witness
     * appears in one of the nodes along the path between ancestor and node (inclusive).
     *
     * Next eventualities:
     *  - X(F φ)  → φ must occur on the path
     *  - X(α U β) → β must occur on the path
     */
    public static boolean hasAncestorWithSameEntriesAndFulfilledNextEventualities(TableauNode node) {
        if (node == null) return false;

        for (TableauNode anc = node.getParent(); anc != null; anc = anc.getParent()) {
            if (!haveSameEntries(node, anc)) continue;

            java.util.Set<IFormula> targets = collectNextEventualityTargets(anc);
            if (targets.isEmpty()) {
                // Vacuously satisfied: same label and no pending next-eventualities
                return true;
            }

            if (targetsFulfilledOnPath(anc, node, targets)) {
                return true;
            }
        }
        return false;
    }

    // Note: Demo main removed to avoid unused variable warnings in strict builds.

    // Merge duplicate entries (same formula string signature). If duplicates exist,
    // prefer setting checked=true if any occurrence is checked.
    private static List<TableauEntry> mergeDedup(List<TableauEntry> entries) {
        java.util.Map<String, TableauEntry> map = new java.util.LinkedHashMap<>();
        for (TableauEntry e : entries) {
            String key = sig(e.getFormula());
            TableauEntry existing = map.get(key);
            if (existing == null) {
                map.put(key, e);
            } else {
                if (e.isChecked()) {
                    existing.setChecked(true);
                }
            }
        }
        return new java.util.ArrayList<>(map.values());
    }
}
