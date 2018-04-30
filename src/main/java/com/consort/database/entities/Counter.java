package com.consort.database.entities;

public class Counter {

    private int value;
    private String name;

    public Counter() {}

    public Counter(final String name, final int value) {
        this.name = name;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
