package com.hamderber.chunklibrary.enums;

public enum AirLossThreshold {
    DEFAULT(-1);

    private final int value;

    AirLossThreshold(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}
