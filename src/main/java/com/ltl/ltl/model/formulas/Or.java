package com.ltl.ltl.model.formulas;

import com.ltl.ltl.model.IFormula;

public class Or implements IFormula {
    private final IFormula left;
    private final IFormula right;

    public Or(IFormula left, IFormula right) {
        this.left = left;
        this.right = right;
    }

    public IFormula getLeft() {
        return left;
    }

    public IFormula getRight() {
        return right;
    }

    @Override
    public String toString() {
        String l = left == null ? "?" : left.toString();
        String r = right == null ? "?" : right.toString();
        return "(" + l + " | " + r + ")";
    }
}
