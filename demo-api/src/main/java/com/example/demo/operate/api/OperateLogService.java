package com.example.demo.operate.api;

import com.example.demo.operate.api.bo.OperateLog;

import java.math.BigInteger;
import java.util.List;

public interface OperateLogService {

    long countLog(OperateLog operateLog);

    List<OperateLog> listLogs(OperateLog operateLog);

    OperateLog getLog(String id);

    OperateLog getLog(BigInteger id);

    OperateLog insertLog(OperateLog operateLog, String creator);

}
