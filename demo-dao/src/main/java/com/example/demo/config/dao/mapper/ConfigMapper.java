package com.example.demo.config.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.config.dao.dataobject.ConfigDO;

import java.util.List;

public interface ConfigMapper extends BaseMapper<ConfigDO> {

    long count(ConfigDO configDO);

    List<ConfigDO> list(ConfigDO configDO);

}
