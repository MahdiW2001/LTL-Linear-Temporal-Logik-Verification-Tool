package com.ltl.ltl.service.syntax;

public class ParseException extends Exception {
    public final int position;
    public ParseException(String message, int position) {
        super(message + " (bei Pos " + position + ")");
        this.position = position;
    }
}
