package com.ltl.ltl.model.tableau;

 import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.ltl.ltl.model.AtomicProposition;
import com.ltl.ltl.model.IFormula;
import com.ltl.ltl.model.Symbol;
import com.ltl.ltl.model.formulas.And;
import com.ltl.ltl.model.formulas.Next;
import com.ltl.ltl.model.formulas.Not;
import com.ltl.ltl.service.tableau.TableauProof;
public class TableauNode {
    private List<TableauEntry> entries;
    private List<TableauNode> children = new ArrayList<>();
    private TableauNode parent;
    private boolean isRoot = false;
    // Nullable closed flag: null = undecided, true = closed/accepted, false = closed/rejected
    private Boolean isClosed; 

    public TableauNode(List<TableauEntry> entries, TableauNode parent) {
        this.entries = entries;
        this.parent = parent;
    }
    //constructur for the first node
    public TableauNode(List<TableauEntry> entries) {
        this.entries = entries;
    }

    public boolean isPoised() {
        for (TableauEntry entry : entries) {
            IFormula formula = entry.getFormula();
            // Check if the formula is an atomic proposition, starts with Next, or is a Not
            if (!(formula instanceof AtomicProposition || formula instanceof Next || formula instanceof Not) && !entry.isChecked())  {
                return false;
            }
        }
        return true;
    }

    public boolean contradiction() {
        Set<Symbol> positives = EnumSet.noneOf(Symbol.class);
        Set<Symbol> negatives = EnumSet.noneOf(Symbol.class);

        for (TableauEntry entry : entries) {
            IFormula f = entry.getFormula();
            if (f == null) continue;

            if (f instanceof AtomicProposition ap) {
                positives.add(ap.getSymbol());
                
            } else if (f instanceof Not n && n.getFormula() instanceof AtomicProposition ap2) {
                
                negatives.add(ap2.getSymbol());
                

            }
        }
        // Contradiction if any atom appears both positively and negatively
        for (Symbol s : positives) {
            if (negatives.contains(s)) return true;
        }
        return false;
    }


    public boolean containsNextOperator() {
        for (TableauEntry entry : entries) {
            if (entry.getFormula() instanceof Next) {
                return true;
            }
        }
        return false;
    }

    public List<TableauEntry> getEntries() {
        return entries;
    }

    public List<TableauNode> getChildren() {
        return children;
    }
    
    public TableauNode getParent() {
        return parent;
    }
    public void setParent(TableauNode parent) {
        this.parent = parent;
    }
    public boolean isRoot() {
        return isRoot;
    }

    public Boolean getIsClosed() {
        return isClosed;
    }

    public void setIsClosed(Boolean isClosed) {
        this.isClosed = isClosed;
    }

    //main function
    public static void main(String[] args) {
       // Create a formula: Globally(A)
        TableauEntry entry = new TableauEntry(new And(
            new AtomicProposition(Symbol.A),
            new Not(new AtomicProposition(Symbol.A))));

        // Create a TableauNode with the formula
        TableauNode rootNode = new TableauNode(new ArrayList<>(List.of(entry)));
        TableauNode expandedNode = TableauProof.expandNodeNoBranching(rootNode, entry);
        for (TableauEntry e : expandedNode.getEntries()) {
            System.out.println("Expanded entry: " + e.getFormula());
        }
        System.out.println("Contradiction found: " + expandedNode.contradiction());
    }
}
