package com.ltl.ltl.model.formulas;
import com.ltl.ltl.model.IFormula;

public class Not implements IFormula {

    private final IFormula formula;

    public Not(IFormula formula) {
        this.formula = formula;
    }

    public IFormula getFormula() {
        return formula;
    }

    @Override
    public String toString() {
        return "!" + (formula == null ? "?" : wrapIfBinary(formula));
    }

    private String wrapIfBinary(IFormula f) {
        if (f instanceof And || f instanceof Or || f instanceof Impl || f instanceof RImpl || f instanceof Equiv || f instanceof Until || f instanceof Release) {
            return "(" + f.toString() + ")";
        }
        return f.toString();
    }
}
