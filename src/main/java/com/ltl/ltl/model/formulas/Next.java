package com.ltl.ltl.model.formulas;
import com.ltl.ltl.model.IFormula;

public class Next implements IFormula {
    private IFormula formula;

    public Next(IFormula formula) {
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
        return "X " + (formula == null ? "?" : formula.toString());
    }
}
