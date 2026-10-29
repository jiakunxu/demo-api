package com.example.demo.login.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.login.dao.dataobject.LoginLogDO;

import java.util.List;

public interface LoginLogMapper extends BaseMapper<LoginLogDO> {

    Long count(LoginLogDO loginLogDO);

    List<LoginLogDO> list(LoginLogDO loginLogDO);

}
