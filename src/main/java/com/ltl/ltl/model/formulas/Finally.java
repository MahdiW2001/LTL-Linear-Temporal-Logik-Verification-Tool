package com.ltl.ltl.model.formulas;
import com.ltl.ltl.model.IFormula;

public class Finally implements IFormula {
    private IFormula formula;

    public Finally(IFormula formula) {
        this.formula = formula;
    }

    public IFormula getFormula() {
        return formula;
    }

    public void setFormula(IFormula formula) {
        this.formula = formula;
    }

    @Override
    public String toString() {
        return "F " + (formula == null ? "?" : formula.toString());
    }
}
