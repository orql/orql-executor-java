package com.github.orql.executor.orql;

public enum TokenType {
    /**
     * op keyword
     * add update delete query count
     */
    OP,
    /**
     * {
     */
    OPEN_CURLY,
    /**
     * }
     */
    CLOSE_CURLY,
    /**
     * [
     */
    OPEN_BRACKET,
    /**
     * ]
     */
    CLOSE_BRACKET,
    /**
     * (
     */
    OPEN_PAREN,
    /**
     * )
     */
    CLOSE_PAREN,
    /**
     * =
     */
    EQ,
    /**
     * !=
     */
    NE,
    /**
     * >
     */
    GT,
    /**
     * >=
     */
    GE,
    /**
     * <
     */
    LT,
    /**
     * <=
     */
    LE,
    /**
     * like
     */
    LIKE,
    /**
     * !
     */
    NOT,
    /**
     * and
     */
    AND,
    /**
     * or
     */
    OR,
    /**
     * order
     */
    ORDER,
    /**
     * name
     */
    NAME,
    /**
     * param
     */
    PARAM,
    /**
     * *
     */
    ALL,
    /**
     * int
     */
    INT,
    /**
     * float
     */
    FLOAT,
    /**
     * sort
     * asc desc
     */
    SORT,
    /**
     * boolean
     * true false
     */
    BOOLEAN,
    /**
     * string
     */
    STRING,
    /**
     * null
     */
    NULL,
    /**
     * ,
     */
    COMMA,
    /**
     * :
     */
    COLON,
    /**
     * eof
     */
    EOF
}
