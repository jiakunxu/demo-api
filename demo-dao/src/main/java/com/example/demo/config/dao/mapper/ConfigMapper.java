package com.example.demo.config.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.config.dao.dataobject.ConfigDO;

import java.util.List;

public interface ConfigMapper extends BaseMapper<ConfigDO> {

    long countConfig(ConfigDO configDO);

    List<ConfigDO> listConfigs(ConfigDO configDO);

    ConfigDO getConfig(ConfigDO configDO);

}
