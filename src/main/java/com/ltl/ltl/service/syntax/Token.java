package com.ltl.ltl.service.syntax;

public class Token {
    private final TokenType type;
    private final String text;
    private final int pos;
    
    public Token(TokenType type, String text, int pos) {
        this.type = type;
        this.text = text;
        this.pos = pos;
    }
    @Override
    public String toString() { return type + (text != null ? "(" + text + ")" : ""); }
    public TokenType getType() {
        return type;
    }
    public String getText() {
        return text;
    }
    public int getPos() {
        return pos;
    }


    
}
