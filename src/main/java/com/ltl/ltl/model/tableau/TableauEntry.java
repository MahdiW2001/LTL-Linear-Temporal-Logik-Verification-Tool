package com.ltl.ltl.model.tableau;

import com.ltl.ltl.model.IFormula;
import com.ltl.ltl.service.syntax.Nnf;

/** Simple holder for a formula plus a 'checked' flag used in tableau expansion. */
public class TableauEntry {

	private final IFormula formula;
	private boolean checked;

	public TableauEntry(IFormula formula) {
		this.formula = new Nnf().toNnf(formula);
        this.checked = false;
	}

	public TableauEntry(IFormula formula, boolean checked) {
		this.formula = new Nnf().toNnf(formula);
		this.checked = checked;
	}

	public IFormula getFormula() {
		return formula;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public void markChecked() { this.checked = true; }
}
