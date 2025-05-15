package com.hamderber.chunklibrary.enums;

// https://stackoverflow.com/questions/3990319/storing-integer-values-as-constants-in-enum-manner-in-java
public enum AirEstimate {
    DEFAULT(-1);

    private final int value;

    AirEstimate(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}
