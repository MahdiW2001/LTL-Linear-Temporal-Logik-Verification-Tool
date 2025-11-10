
package com.ltl.ltl.service.syntax;

/**
 * Lexer for LTL formulas. Converts a formula string into a stream of tokens for parsing.
 * Supports atomic propositions (a, b, c), parentheses, unary and binary LTL operators.
 */
public class Lexer {
    private final String s;
    private int i = 0;

    /**
     * Constructs a lexer for the given input string.
     * Tokenizes Input
     * @param s the LTL formula string to tokenize
     */
    public Lexer(String s) { this.s = s; }
    
    /**
     * Returns the next token in the input string, skipping whitespace.
     * @return the next Token
     * @throws ParseException if an unexpected character is encountered
     */
    public Token next() throws ParseException {
        skipWs();
        if (i >= s.length()) return new Token(TokenType.EOF, null, i);
        char c = s.charAt(i);
        int start = i;

        if (c == 'a' || c == 'b' || c == 'c') {
            i++;
            return new Token(TokenType.PROP, String.valueOf(c), start);
        }
        if (c == '(') { i++; return new Token(TokenType.LPAREN, "(", start); }
        if (c == ')') { i++; return new Token(TokenType.RPAREN, ")", start); }
        if (c == 'G') { i++; return new Token(TokenType.G, "G", start); }
        if (c == 'X') { i++; return new Token(TokenType.X, "X", start); }
        if (c == 'F') { i++; return new Token(TokenType.F, "F", start); }
        if (c == 'W') { i++; return new Token(TokenType.W, "W", start); }
        if (c == 'U') { i++; return new Token(TokenType.U, "U", start); }
        if (c == 'R') { i++; return new Token(TokenType.R, "R", start); }
        if (c == '&') { i++; return new Token(TokenType.AND, "&", start); }
        if (c == '|') { i++; return new Token(TokenType.OR, "|", start); }
        if (c == '>') { i++; return new Token(TokenType.IMPL, ">", start); }
        if (c == '<') { i++; return new Token(TokenType.RIMPL, "<", start); }
        if (c == '=') { i++; return new Token(TokenType.EQUIV, "=", start); }
        if (c == '!') { i++; return new Token(TokenType.NOT, "!", start); }

    throw new ParseException("Unerwartetes Zeichen: '" + c + "'", start);
    }

    /**
     * Skips whitespace characters in the input string.
     */
    private void skipWs() {
        while (i < s.length()) {
            char ch = s.charAt(i);
            if (Character.isWhitespace(ch)) { i++; }
            else break;
        }
    }
}
