package com.aplana.timesheet.util;

public class Pair<P, Q> {
    private P first;
    private Q second;

    public Pair(P first, Q second) {
        this.first = first;
        this.second = second;
    }

    public P getFirst() {
        return first;
    }

    public void setFirst(P first) {
        this.first = first;
    }

    public Q getSecond() {
        return second;
    }

    public void setSecond(Q second) {
        this.second = second;
    }
}
