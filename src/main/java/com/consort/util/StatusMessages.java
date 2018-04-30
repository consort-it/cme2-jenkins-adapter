package com.consort.util;

public enum StatusMessages {

    NOT_AUTHORIZED("Access denied!"),
    INSUFFICIENT_DATA("Error! Insufficient data provided!"),
    COULD_NOT_EXPORT_EXCEL("Error! Could not export to excel!"),
    EXCEL_EXPORTED("Excel exported!"),
    INSERT_SUCCESSFUL("Insert successful"),
    DATA_VALIDATION_FAILED("Timesheet json has wrong format!"),
    GENERAL_ERROR("Error!");

    private String value;

    StatusMessages(final String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
