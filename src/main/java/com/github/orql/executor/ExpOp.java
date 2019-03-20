package com.github.orql.executor;

public enum ExpOp {
    And("&&"),
    Or("||"),
    Gt(">"),
    Lt("<"),
    Ge(">="),
    Le("<="),
    Ne("!="),
    Eq("="),
    Like("like");

    private String text;

    ExpOp(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

    public static ExpOp fromText(String text) {
        for (ExpOp op : ExpOp.values()) {
            if (op.text.equals(text)) return op;
        }
        return null;
    }
}