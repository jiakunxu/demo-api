package com.example.demo.config.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.demo.config.api.ConfigService;
import com.example.demo.config.api.bo.Config;
import com.example.demo.config.dao.dataobject.ConfigDO;
import com.example.demo.config.dao.mapper.ConfigMapper;
import com.example.demo.framework.annotation.NotBlank;
import com.example.demo.framework.annotation.NotNull;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, ConfigDO>
                               implements ConfigService {

    @Override
    public long countConfig(Config config) {
        if (config == null) {
            return 0;
        }

        return this.baseMapper.countConfig(BeanUtil.copy(config, ConfigDO.class));
    }

    @Override
    public List<Config> listConfigs(Config config) {
        if (config == null) {
            return List.of();
        }

        List<Config> list = BeanUtil
            .copy(this.baseMapper.listConfigs(BeanUtil.copy(config, ConfigDO.class)), Config.class);

        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }

        return list;
    }

    @Override
    public Config getConfig(String id, String key) {
        if (StringUtils.isAllBlank(id, key)) {
            return null;
        }

        ConfigDO configDO = new ConfigDO();

        if (StringUtils.isNotBlank(id)) {
            configDO.setId(new BigInteger(id));
        }

        configDO.setKey(key);

        return BeanUtil.copy(this.baseMapper.getConfig(configDO), Config.class);
    }

    @Override
    public Config insertConfig(@NotNull Config config, @NotBlank String creator) {
        ConfigDO configDO = BeanUtil.copy(config, ConfigDO.class);
        configDO.setCreator(creator);

        this.save(configDO);

        config.setId(configDO.getId());

        return config;
    }

    @Override
    public Config updateConfig(@NotNull BigInteger id, @NotNull Config config,
                               @NotBlank String modifier) {
        config.setId(id);

        ConfigDO configDO = BeanUtil.copy(config, ConfigDO.class);
        configDO.setModifier(modifier);

        if (!this.updateById(configDO)) {
            log.error("{}", configDO);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        return config;
    }

    @Override
    public Config deleteConfig(@NotNull BigInteger id, @NotBlank String modifier) {
        var updateWrapper = Wrappers.<ConfigDO> lambdaUpdate().eq(ConfigDO::getId, id)
            .set(ConfigDO::getDeleted, true).set(ConfigDO::getModifier, modifier);

        if (!this.update(updateWrapper)) {
            log.error("{},{}", id, modifier);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        return new Config(id);
    }

}
