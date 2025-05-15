package com.hamderber.chunklibrary.enums;

public enum RegenPeriod {
    DISABLED(-1);

    private final int value;

    RegenPeriod(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}
