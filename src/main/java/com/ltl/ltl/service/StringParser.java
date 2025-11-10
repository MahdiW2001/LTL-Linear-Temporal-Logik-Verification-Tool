package com.ltl.ltl.service;

import com.ltl.ltl.model.AtomicProposition;
import com.ltl.ltl.model.IFormula;
import com.ltl.ltl.model.formulas.And;
import com.ltl.ltl.model.formulas.Equiv;
import com.ltl.ltl.model.formulas.Finally;
import com.ltl.ltl.model.formulas.Globally;
import com.ltl.ltl.model.formulas.Impl;
import com.ltl.ltl.model.formulas.Next;
import com.ltl.ltl.model.formulas.Not;
import com.ltl.ltl.model.formulas.Or;
import com.ltl.ltl.model.formulas.RImpl;
import com.ltl.ltl.model.formulas.Release;
import com.ltl.ltl.model.formulas.Until;
import com.ltl.ltl.model.formulas.WeakNext;

public class StringParser {

    public String toString(IFormula formula) {
        if (formula instanceof AtomicProposition ap) {
            // atomare Propositionen ohne Klammern
            return ap.getSymbol().name().toLowerCase();
        }
        if (formula instanceof Not n) {
            // Not bekommt immer ein "!"
            IFormula inner = n.getFormula();
            // bei komplexeren Formeln Klammern drum
            if (inner instanceof AtomicProposition) {
                return "!" + toString(inner);
            } else {
                return "!(" + toString(inner) + ")";
            }
        }
        if (formula instanceof Globally g) {
            return "G" + wrap(g.getFormula());
        }
        if (formula instanceof Finally f) {
            return "F" + wrap(f.getFormula());
        }
        if (formula instanceof Next x) {
            return "X" + wrap(x.getFormula());
        }
        if (formula instanceof WeakNext w) {
            return "W" + wrap(w.getFormula());
        }
        if (formula instanceof Until u) {
            return wrap(u.getLeft()) + "U" + wrap(u.getRight());
        }
        if (formula instanceof Release r) {
            return wrap(r.getLeft()) + "R" + wrap(r.getRight());
        }
        if (formula instanceof And a) {
            return wrap(a.getLeft()) + "&" + wrap(a.getRight());
        }
        if (formula instanceof Or o) {
            return wrap(o.getLeft()) + "|" + wrap(o.getRight());
        }
        if (formula instanceof Impl imp) {
            return wrap(imp.getLeft()) + ">" + wrap(imp.getRight());
        }
        if (formula instanceof RImpl ri) {
            return wrap(ri.getLeft()) + "<" + wrap(ri.getRight());
        }
        if (formula instanceof Equiv eq) {
            return wrap(eq.getLeft()) + "=" + wrap(eq.getRight());
        }
        throw new IllegalArgumentException("Unbekannter Formeltyp: " + formula.getClass());
    }

    private String wrap(IFormula f) {
        // atomare bleiben ohne Klammern
        if (f instanceof AtomicProposition) {
            return toString(f);
        }
        if (f instanceof Not n && n.getFormula() instanceof AtomicProposition) {
            // auch !a ohne zusätzliche ()
            return toString(f);
        }
        // alles andere in () setzen
        return "(" + toString(f) + ")";
    }
}
