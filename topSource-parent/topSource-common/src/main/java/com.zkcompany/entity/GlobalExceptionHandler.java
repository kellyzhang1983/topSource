package com.zkcompany.entity;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public Result error(BusinessException e){
        String customMessage = e.getCustomMessage() == null ? "" : e.getCustomMessage();
        return  new Result(false,e.getCode(), "SystemExceptionMessage：" +  e.getMessage() +  "；<br>BusinessException：" +  customMessage );
    }
}
