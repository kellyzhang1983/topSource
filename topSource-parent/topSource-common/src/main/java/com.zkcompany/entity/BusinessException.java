package com.zkcompany.entity;

public class BusinessException extends RuntimeException {
    private final int code;  // 自定义错误码
    private final String message;
    private String customMessage;//自定义错误信息

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(int code, String message,String customMessage) {
        super(message);
        this.code = code;
        this.message = message;
        this.customMessage = customMessage;
    }

    // Getters
    public int getCode() {
        return code;
    }

    // Getters
    public String getCustomMessage() {
        return customMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
