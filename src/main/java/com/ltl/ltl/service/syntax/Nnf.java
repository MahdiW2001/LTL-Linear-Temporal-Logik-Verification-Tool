package com.ltl.ltl.service.syntax;

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
import com.ltl.ltl.service.AstParser;
import com.ltl.ltl.service.StringParser;

public class Nnf {

    private final StringParser stringParser = new StringParser();

    /**
     * Transformiert eine Formel in NNF.
     */
    public IFormula toNnf(IFormula formula) {
        java.util.IdentityHashMap<IFormula, Boolean> seen = new java.util.IdentityHashMap<>();
        return toNnfRec(formula, seen);
    }

    private IFormula toNnfRec(IFormula formula, java.util.IdentityHashMap<IFormula, Boolean> seen) {
        if (formula == null) return null;
        if (seen.containsKey(formula)) {
            // Cycle detected in formula graph; return as-is to avoid infinite recursion
            return formula;
        }
        seen.put(formula, Boolean.TRUE);

        if (formula instanceof Not n) {
            // push negation inside
            return toNegNnfRec(n.getFormula(), seen);
        }
        if (formula instanceof AtomicProposition) {
            return formula; // already an atom
        }
        if (formula instanceof Globally g) {
            return new Globally(toNnfRec(g.getFormula(), seen));
        }
        if (formula instanceof Finally f) {
            return new Finally(toNnfRec(f.getFormula(), seen));
        }
        if (formula instanceof Next x) {
            return new Next(toNnfRec(x.getFormula(), seen));
        }
        if (formula instanceof WeakNext w) {
            return new WeakNext(toNnfRec(w.getFormula(), seen));
        }
        if (formula instanceof Until u) {
            return new Until(toNnfRec(u.getLeft(), seen), toNnfRec(u.getRight(), seen));
        }
        if (formula instanceof Release r) {
            return new Release(toNnfRec(r.getLeft(), seen), toNnfRec(r.getRight(), seen));
        }
        if (formula instanceof And a) {
            return new And(toNnfRec(a.getLeft(), seen), toNnfRec(a.getRight(), seen));
        }
        if (formula instanceof Or o) {
            return new Or(toNnfRec(o.getLeft(), seen), toNnfRec(o.getRight(), seen));
        }
        if (formula instanceof Impl imp) { // a -> b  === ¬a ∨ b
            return new Or(toNegNnfRec(imp.getLeft(), seen), toNnfRec(imp.getRight(), seen));
        }
        if (formula instanceof RImpl ri) { // a <- b  === b -> a === ¬b ∨ a
            return new Or(toNegNnfRec(ri.getRight(), seen), toNnfRec(ri.getLeft(), seen));
        }
        if (formula instanceof Equiv eq) { // a <-> b === (a & b) | (¬a & ¬b)
            IFormula aPos = toNnfRec(eq.getLeft(), seen);
            IFormula bPos = toNnfRec(eq.getRight(), seen);
            IFormula leftAnd = new And(aPos, bPos);
            IFormula rightAnd = new And(toNegNnfRec(eq.getLeft(), seen), toNegNnfRec(eq.getRight(), seen));
            return new Or(leftAnd, rightAnd);
        }
        // fallback (should not happen if all classes covered)
        return formula;
    }

    /**
     * Helper: returns NNF of the negation of the given formula.
     */
    private IFormula toNegNnf(IFormula formula) {
        java.util.IdentityHashMap<IFormula, Boolean> seen = new java.util.IdentityHashMap<>();
        return toNegNnfRec(formula, seen);
    }

    private IFormula toNegNnfRec(IFormula formula, java.util.IdentityHashMap<IFormula, Boolean> seen) {
        if (formula == null) return null;
        if (seen.containsKey(formula)) {
            return new Not(formula);
        }
        seen.put(formula, Boolean.TRUE);

        if (formula instanceof AtomicProposition ap) {
            return new Not(ap); // negated atom stays as Not(atom)
        }
        if (formula instanceof Not n) { // ¬¬φ => NNF(φ)
            return toNnfRec(n.getFormula(), seen);
        }
        if (formula instanceof Globally g) { // ¬Gφ => F¬φ
            return new Finally(toNegNnfRec(g.getFormula(), seen));
        }
        if (formula instanceof Finally f) { // ¬Fφ => G¬φ
            return new Globally(toNegNnfRec(f.getFormula(), seen));
        }
        if (formula instanceof Next x) { // ¬Xφ => X¬φ
            return new Next(toNegNnfRec(x.getFormula(), seen));
        }
        if (formula instanceof WeakNext w) { // treat like next: ¬Wφ => W¬φ
            return new WeakNext(toNegNnfRec(w.getFormula(), seen));
        }
        if (formula instanceof Until u) { // ¬(a U b) => ¬a R ¬b
            return new Release(toNegNnfRec(u.getLeft(), seen), toNegNnfRec(u.getRight(), seen));
        }
        if (formula instanceof Release r) { // ¬(a R b) => ¬a U ¬b
            return new Until(toNegNnfRec(r.getLeft(), seen), toNegNnfRec(r.getRight(), seen));
        }
        if (formula instanceof And a) { // ¬(a & b) => ¬a | ¬b
            return new Or(toNegNnfRec(a.getLeft(), seen), toNegNnfRec(a.getRight(), seen));
        }
        if (formula instanceof Or o) { // ¬(a | b) => ¬a & ¬b
            return new And(toNegNnfRec(o.getLeft(), seen), toNegNnfRec(o.getRight(), seen));
        }
        if (formula instanceof Impl imp) { // ¬(a -> b) => a & ¬b
            return new And(toNnfRec(imp.getLeft(), seen), toNegNnfRec(imp.getRight(), seen));
        }
        if (formula instanceof RImpl ri) { // ¬(a <- b) meaning ¬(b -> a) => b & ¬a
            return new And(toNnfRec(ri.getRight(), seen), toNegNnfRec(ri.getLeft(), seen));
        }
        if (formula instanceof Equiv eq) { // ¬(a <-> b) => (a & ¬b) | (¬a & b)
            IFormula aPos = toNnfRec(eq.getLeft(), seen);
            IFormula bPos = toNnfRec(eq.getRight(), seen);
            IFormula leftAnd = new And(aPos, toNegNnfRec(eq.getRight(), seen));
            IFormula rightAnd = new And(toNegNnfRec(eq.getLeft(), seen), bPos);
            return new Or(leftAnd, rightAnd);
        }
        // fallback: wrap in Not (should not occur if exhaustive)
        return new Not(formula);
    }

    /**
     * Convenience: nimmt einen String, parsed zu IFormula und wandelt in NNF zurück als String.
     */
    public String toNnf(String formulaString) throws ParseException {
        AstParser parser = new AstParser(new Lexer(formulaString));
        IFormula formula = parser.parseFormula();
        IFormula nnfFormula = toNnf(formula);
        return stringParser.toString(nnfFormula);
    }
}
