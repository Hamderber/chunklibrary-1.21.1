package com.hamderber.chunklibrary.enums;

public enum AgeLimit {
    DEFAULT(-1);

    private final int value;

    AgeLimit(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}
