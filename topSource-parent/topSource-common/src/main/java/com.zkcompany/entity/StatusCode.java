package com.zkcompany.entity;

import org.apache.http.HttpStatus;

/**
 * 返回码
 */
public class StatusCode implements HttpStatus {
    public static final int OK = 20000;//成功
    public static final int ERROR = 20001;//失败
    public static final int LOGINERROR = 20002;//用户名或密码错误
    public static final int ACCESSERROR = 20003;//权限不足
    public static final int REMOTEERROR = 20004;//远程调用失败
    public static final int REPERROR = 20005;//重复操作
    public static final int NOTOKEN = 20006;//没有token
    public static final int RATELIMIT = 20007;//没有对应的抢购数据

}
