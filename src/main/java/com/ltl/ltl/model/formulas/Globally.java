package com.ltl.ltl.model.formulas;
import com.ltl.ltl.model.IFormula;

public class Globally implements IFormula {
    private IFormula formula;

    public Globally(IFormula formula) {
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
        return "G " + (formula == null ? "?" : formula.toString());
    }
}
