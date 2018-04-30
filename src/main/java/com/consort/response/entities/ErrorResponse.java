package com.consort.response.entities;

public class ErrorResponse {


    public static final String CODE_PREFIX = "JENK-";
    private String message;
    private String code;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = CODE_PREFIX + code;
    }

    public ErrorResponse(String message, String code){
        this.message = message;
        setCode(code);
    }
}
