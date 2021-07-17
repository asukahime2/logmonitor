package com.asukahime.logmonitor;

import java.io.Serializable;

public class Pair<L, R> implements Serializable {
    private static final long serialVersionUID = 1L;

    private L left;

    private R right;

    private Pair(){}

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }
}
