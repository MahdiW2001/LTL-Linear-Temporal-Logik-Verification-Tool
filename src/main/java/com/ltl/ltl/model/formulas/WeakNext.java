package com.ltl.ltl.model.formulas;
import com.ltl.ltl.model.IFormula;

public class WeakNext implements IFormula {
    private IFormula formula;

    public WeakNext(IFormula formula) {
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
        return "WX " + (formula == null ? "?" : formula.toString());
    }
}
