package com.github.orql.executor;

/**
 * 级联操作
 */
public enum Cascade {
    /**
     * 限制
     */
    Restrict,
    /**
     * 同步
     */
    NoAction,
    /**
     * 级联
     */
    Cascade,
    /**
     * 设null
     */
    SetNull
}
