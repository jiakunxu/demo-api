package com.example.demo.login.service;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.demo.framework.annotation.NotBlank;
import com.example.demo.framework.annotation.NotNull;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.login.api.LoginLogService;
import com.example.demo.login.api.bo.LoginLog;
import com.example.demo.login.dao.dataobject.LoginLogDO;
import com.example.demo.login.dao.mapper.LoginLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLogDO>
                                 implements LoginLogService {

    @Override
    public long countLog(LoginLog log) {
        if (log == null) {
            return 0;
        }

        return this.getBaseMapper().count(BeanUtil.copy(log, LoginLogDO.class));
    }

    @Override
    public List<LoginLog> listLogs(LoginLog log) {
        if (log == null) {
            return List.of();
        }

        if (StringUtils.isAnyBlank(log.getSort(), log.getDir())) {
            log.setSort("id");
            log.setDir("DESC");
        }

        List<LoginLog> list = BeanUtil
            .copy(this.getBaseMapper().list(BeanUtil.copy(log, LoginLogDO.class)), LoginLog.class);

        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }

        return list;
    }

    @Override
    public LoginLog getLog(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }

        return getLog(new BigInteger(id));
    }

    @Override
    public LoginLog getLog(BigInteger id) {
        if (id == null) {
            return null;
        }

        return BeanUtil.copy(this.getById(id), LoginLog.class);
    }

    @Override
    public void insertLog(@NotNull LoginLog log, @NotBlank String createBy) {
        LoginLogDO logDO = BeanUtil.copy(log, LoginLogDO.class);
        logDO.setCreateBy(createBy);

        this.save(logDO);

        log.setId(logDO.getId());
    }

}
