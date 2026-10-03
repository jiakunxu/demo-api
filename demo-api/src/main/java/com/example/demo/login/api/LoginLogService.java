package com.example.demo.login.api;

import com.example.demo.login.api.bo.LoginLog;

import java.math.BigInteger;
import java.util.List;

public interface LoginLogService {

    long countLog(LoginLog loginLog);

    List<LoginLog> listLogs(LoginLog loginLog);

    LoginLog getLog(String id);

    LoginLog getLog(BigInteger id);

    LoginLog insertLog(LoginLog loginLog, String creator);

}
