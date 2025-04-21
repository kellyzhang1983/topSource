package com.zkcompany.entity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public Result error(BusinessException e){
        String customMessage = e.getCustomMessage() == null ? "" : e.getCustomMessage();
        log.error("SystemExceptionMessage：" +  e.getMessage() +  "；BusinessException：" +  customMessage );
        return  new Result(false,e.getCode(), "SystemExceptionMessage：" +  e.getMessage() +  "；<br>BusinessException：" +  customMessage );
    }
}
