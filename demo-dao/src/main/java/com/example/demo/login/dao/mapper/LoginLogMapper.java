package com.example.demo.login.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.login.dao.dataobject.LoginLogDO;

import java.util.List;

public interface LoginLogMapper extends BaseMapper<LoginLogDO> {

    int countLog(LoginLogDO loginLogDO);

    List<LoginLogDO> listLogs(LoginLogDO loginLogDO);

}
