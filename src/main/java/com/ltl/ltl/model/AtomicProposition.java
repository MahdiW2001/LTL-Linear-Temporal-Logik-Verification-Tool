package com.ltl.ltl.model;

public class AtomicProposition implements IFormula{
    private Symbol symbol;

    public AtomicProposition(Symbol symbol) {
        this.symbol = symbol;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol != null ? symbol.name() : "?";
    }
}
