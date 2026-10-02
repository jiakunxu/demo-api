package com.example.demo.log.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.framework.annotation.NotBlank;
import com.example.demo.framework.annotation.NotNull;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.operate.api.OperateLogService;
import com.example.demo.operate.api.bo.OperateLog;
import com.example.demo.operate.dao.dataobject.OperateLogDO;
import com.example.demo.operate.dao.mapper.OperateLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
public class OperateLogServiceImpl extends ServiceImpl<OperateLogMapper, OperateLogDO>
                                   implements OperateLogService {

    @Override
    public int countLog(OperateLog log) {
        if (log == null) {
            return 0;
        }

        return this.baseMapper.countLog(BeanUtil.copy(log, OperateLogDO.class));
    }

    @Override
    public List<OperateLog> listLogs(OperateLog log) {
        if (log == null) {
            return List.of();
        }

        if (StringUtils.isAnyBlank(log.getSort(), log.getDir())) {
            log.setSort("id");
            log.setDir("DESC");
        }

        List<OperateLog> list = BeanUtil.copy(
            this.baseMapper.listLogs(BeanUtil.copy(log, OperateLogDO.class)), OperateLog.class);

        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }

        for (OperateLog item : list) {
            if ("anonymous".equals(item.getOperator())) {
                item.setOperator("");
            }
        }

        return list;
    }

    @Override
    public OperateLog getLog(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }

        OperateLog log = getLog(new BigInteger(id));

        if (log == null) {
            return null;
        }

        if ("anonymous".equals(log.getOperator())) {
            log.setOperator("");
        }

        return log;
    }

    @Override
    public OperateLog getLog(BigInteger id) {
        if (id == null) {
            return null;
        }

        return BeanUtil.copy(this.getById(id), OperateLog.class);
    }

    @Override
    public OperateLog insertLog(@NotNull OperateLog operateLog, @NotBlank String creator) {
        OperateLogDO logDO = BeanUtil.copy(operateLog, OperateLogDO.class);
        logDO.setCreator(creator);

        this.save(logDO);

        operateLog.setId(logDO.getId());

        return operateLog;
    }

}
