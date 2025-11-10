package com.ltl.ltl.service;

import com.ltl.ltl.model.AtomicProposition;
import com.ltl.ltl.model.IFormula;
import com.ltl.ltl.model.Symbol;
import com.ltl.ltl.model.formulas.*;
import com.ltl.ltl.service.syntax.Lexer;
import com.ltl.ltl.service.syntax.ParseException;
import com.ltl.ltl.service.syntax.Token;
import com.ltl.ltl.service.syntax.TokenType;

public class AstParser {
    private final Lexer lx;
    private Token lookahead;

    public AstParser(Lexer lx) throws ParseException {
        this.lx = lx;
        this.lookahead = lx.next();
    }

    public IFormula parseFormula() throws ParseException {
        IFormula f = parseEquiv();
        expect(TokenType.EOF);
        return f;
    }

    // equiv := impl ('=' impl)*
    private IFormula parseEquiv() throws ParseException {
        IFormula left = parseImpl();
        while (lookahead.getType() == TokenType.EQUIV) {
            consume(TokenType.EQUIV);
            IFormula right = parseImpl();
            left = new Equiv(left, right);
        }
        return left;
    }

    // impl := orExpr (('>' | '<') orExpr)*
    private IFormula parseImpl() throws ParseException {
        IFormula left = parseOr();
        while (lookahead.getType() == TokenType.IMPL || lookahead.getType() == TokenType.RIMPL) {
            if (lookahead.getType() == TokenType.IMPL) {
                consume(TokenType.IMPL);
                IFormula right = parseOr();
                left = new Impl(left, right);
            } else {
                consume(TokenType.RIMPL);
                IFormula right = parseOr();
                left = new RImpl(left, right);
            }
        }
        return left;
    }

    // orExpr := andExpr ('|' andExpr)*
    private IFormula parseOr() throws ParseException {
        IFormula left = parseAnd();
        while (lookahead.getType() == TokenType.OR) {
            consume(TokenType.OR);
            IFormula right = parseAnd();
            left = new Or(left, right);
        }
        return left;
    }

    // andExpr := untilExpr ('&' untilExpr)*
    private IFormula parseAnd() throws ParseException {
        IFormula left = parseUntilRelease();
        while (lookahead.getType() == TokenType.AND) {
            consume(TokenType.AND);
            IFormula right = parseUntilRelease();
            left = new And(left, right);
        }
        return left;
    }

    // untilRelease := prefix (('U' | 'R') prefix)*
    private IFormula parseUntilRelease() throws ParseException {
        IFormula left = parsePrefix();
        while (lookahead.getType() == TokenType.U || lookahead.getType() == TokenType.R) {
            if (lookahead.getType() == TokenType.U) {
                consume(TokenType.U);
                IFormula right = parsePrefix();
                left = new Until(left, right);
            } else {
                consume(TokenType.R);
                IFormula right = parsePrefix();
                left = new Release(left, right);
            }
        }
        return left;
    }

    // prefix := ('!' | G | X | F | W)* primary
    private IFormula parsePrefix() throws ParseException {
        if (lookahead.getType() == TokenType.NOT) {
            consume(TokenType.NOT);
            return new Not(parsePrefix());
        } else if (lookahead.getType() == TokenType.G) {
            consume(TokenType.G);
            return new Globally(parsePrefix());
        } else if (lookahead.getType() == TokenType.X) {
            consume(TokenType.X);
            return new Next(parsePrefix());
        } else if (lookahead.getType() == TokenType.F) {
            consume(TokenType.F);
            return new Finally(parsePrefix());
        } else if (lookahead.getType() == TokenType.W) {
            consume(TokenType.W);
            return new WeakNext(parsePrefix());
        }
        return parsePrimary();
    }

    // primary := PROP | '(' equiv ')'
    private IFormula parsePrimary() throws ParseException {
        switch (lookahead.getType()) {
            case PROP -> {
                String name = lookahead.getText();
                consume(TokenType.PROP);
                return new AtomicProposition(Symbol.valueOf(name.toUpperCase()));
            }
            case LPAREN -> {
                consume(TokenType.LPAREN);
                IFormula inside = parseEquiv();
                expect(TokenType.RPAREN);
                return inside;
            }
            default -> throw error("Atom oder '(' erwartet, gefunden: " + lookahead);
        }
    }

    private void consume(TokenType expected) throws ParseException {
        if (lookahead.getType() != expected) {
            throw error("Erwartet: " + expected + ", aber gefunden: " + lookahead);
        }
        lookahead = lx.next();
    }

    private void expect(TokenType expected) throws ParseException {
        consume(expected);
    }

    private ParseException error(String msg) {
        return new ParseException(msg, lookahead.getPos());
    }
}
