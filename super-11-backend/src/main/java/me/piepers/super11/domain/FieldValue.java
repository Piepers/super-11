package me.piepers.super11.domain;

/**
 * A simple object that contains a field and a value.
 *
 * @author Bas Piepers
 *
 */
public class FieldValue {
    private final String field;
    private final String value;

    private FieldValue(String field, String value) {
        this.field = field;
        this.value = value;
    }

    public static FieldValue of(String field, String value) {
        return new FieldValue(field, value);
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}
