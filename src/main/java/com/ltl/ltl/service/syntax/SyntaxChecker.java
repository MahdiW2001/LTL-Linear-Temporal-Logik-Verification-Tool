package com.ltl.ltl.service.syntax;

public class SyntaxChecker {
    public boolean isValid(String input) {
        try {
            validateOrThrow(input);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public void validateOrThrow(String input) throws ParseException {
        if (input == null) throw new ParseException("Input is null", 0);
        Parser p = new Parser(new Lexer(input));
        p.parseFormula();
        p.expect(TokenType.EOF);
    }

}