package com.github.orql.executor.orql;

public class Token {

    private TokenType type;

    private String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public Token(TokenType type, char value) {
        this.type = type;
        this.value = String.valueOf(value);
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Token && ((Token) obj).type == type && ((Token) obj).value.equals(value);
    }
}
