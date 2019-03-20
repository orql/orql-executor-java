package com.github.orql.executor.orql;

public class Lexer {

    private String orql;

    private Integer index = 0;

    public Lexer(String orql) {
        this.orql = orql;
    }

    private char currentChar() {
        return orql.charAt(this.index);
    }

    private char nextChar() {
        return orql.charAt(this.index + 1);
    }

    private boolean end() {
        return index >= orql.length();
    }

    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private Token readString(char start) {
        StringBuilder builder = new StringBuilder();
        while (! end()) {
            char c = currentChar();
            if (c == start) {
                index ++;
                break;
            }
            if (c == '\\' && this.nextChar() == start) {
                index += 2;
                builder.append(start);
                continue;
            }
            builder.append(c);
            index ++;
        }
        return new Token(TokenType.STRING, builder.toString());
    }

    private String readName(char start) {
        StringBuilder builder = new StringBuilder();
        builder.append(start);
        while (! end()) {
            char c = currentChar();
            if (! isLetter(c) && c != '_') break;
            index ++;
            builder.append(c);
        }
        return builder.toString();
    }

    private Token readNumber(char start) {
        StringBuilder builder = new StringBuilder();
        builder.append(start);
        boolean dot = false;
        while (! end()) {
            char c = currentChar();
            if (c == '.') {
                if (dot) throw new LexerException("multi dot");
                dot = true;
            }
            if (! isDigit(c) && c != '.') break;
            this.index ++;
            builder.append(c);
        }
        return dot ?
                new Token(TokenType.FLOAT, builder.toString()) :
                new Token(TokenType.INT, builder.toString());
    }

    private Token readParam() {
        char start = orql.charAt(this.index ++);
        String name = readName(start);
        return new Token(TokenType.PARAM, name);
    }

    public Token nextToken() {
        if (end()) return new Token(TokenType.EOF, "EOF");
        char c = orql.charAt(this.index ++);
        switch (c) {
            case ' ':
                return this.nextToken();
            case '*':
                return new Token(TokenType.ALL, c);
            case '{':
                return new Token(TokenType.OPEN_CURLY, c);
            case '}':
                return new Token(TokenType.CLOSE_CURLY, c);
            case '(':
                return new Token(TokenType.OPEN_PAREN, c);
            case ')':
                return new Token(TokenType.CLOSE_PAREN, c);
            case '[':
                return new Token(TokenType.OPEN_BRACKET, c);
            case ']':
                return new Token(TokenType.CLOSE_BRACKET, c);
            case ':':
                return new Token(TokenType.COLON, c);
            case ',':
                return new Token(TokenType.COMMA, c);
            case '=':
                return new Token(TokenType.EQ, c);
            case '>':
                if (currentChar() == '=') {
                    this.index ++;
                    return new Token(TokenType.GE, ">=");
                }
                return new Token(TokenType.GT, ">");
            case '<':
                if (currentChar() == '=') {
                    this.index ++;
                    return new Token(TokenType.LE, "<=");
                }
                return new Token(TokenType.LT, "<");
            case '!':
                if (currentChar() == '=') {
                    this.index ++;
                    return new Token(TokenType.NE, "!=");
                }
                return new Token(TokenType.NOT, "!");
            case '&':
                if (currentChar() == '&') {
                    this.index ++;
                    return new Token(TokenType.AND, "&&");
                }
                throw new LexerException("miss &");
            case '|':
                if (currentChar() == '|') {
                    this.index ++;
                    return new Token(TokenType.OR, "||");
                }
                throw new LexerException("miss |");
            case '\'':
            case '"':
                return readString(c);
            case '$':
            case '#':
                return readParam();
        }
        if (isLetter(c)) {
            String name = this.readName(c);
            switch (name) {
                case "order":
                    return new Token(TokenType.ORDER, "order");
                case "true":
                    return new Token(TokenType.BOOLEAN, "true");
                case "false":
                    return new Token(TokenType.BOOLEAN, "false");
                case "like":
                    return new Token(TokenType.LIKE, "like");
                case "null":
                    return new Token(TokenType.NULL, "null");
                default:
                    return new Token(TokenType.NAME, name);
            }
        }
        if (isDigit(c)) return readNumber(c);
        throw new LexerException("not support char: " + c);
    }

}
