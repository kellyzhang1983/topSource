package com.zkcompany.service;

public interface UserDataBaseAndRedisService {

    boolean checkTbUserToRedis();

    boolean checkTbUserPoint();

    boolean checkTbUserRole();
}
