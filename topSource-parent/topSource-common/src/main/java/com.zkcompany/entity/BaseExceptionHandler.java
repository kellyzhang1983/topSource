package com.zkcompany.entity;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class BaseExceptionHandler {


    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result error(Exception e){
        return  new Result(false,StatusCode.SC_INTERNAL_SERVER_ERROR,e.getMessage());
    }
}
