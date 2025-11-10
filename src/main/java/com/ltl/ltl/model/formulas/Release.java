package com.ltl.ltl.model.formulas;
import com.ltl.ltl.model.IFormula;

public class Release implements IFormula {
    private IFormula left;
    private IFormula right;

    public Release(IFormula left, IFormula right) {
        this.left = left;
        this.right = right;
    }

    public IFormula getLeft() {
        return left;
    }

    public IFormula getRight() {
        return right;
    }

    public void setLeft(IFormula left) {
        this.left = left;
    }

    public void setRight(IFormula right) {
        this.right = right;
    }

    @Override
    public String toString() {
        String l = left == null ? "?" : left.toString();
        String r = right == null ? "?" : right.toString();
        return "(" + l + " R " + r + ")";
    }
}
