package com.example.demo.operate.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.operate.dao.dataobject.OperateLogDO;

import java.util.List;

public interface OperateLogMapper extends BaseMapper<OperateLogDO> {

    long countLog(OperateLogDO operateLogDO);

    List<OperateLogDO> listLogs(OperateLogDO operateLogDO);

}
