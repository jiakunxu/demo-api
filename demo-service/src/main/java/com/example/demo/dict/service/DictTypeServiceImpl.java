package com.example.demo.dict.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.demo.cache.api.RedisService;
import com.example.demo.dict.api.DictDataService;
import com.example.demo.dict.api.DictTypeService;
import com.example.demo.dict.api.bo.DictType;
import com.example.demo.dict.dao.dataobject.DictTypeDO;
import com.example.demo.dict.dao.mapper.DictTypeMapper;
import com.example.demo.framework.annotation.NotBlank;
import com.example.demo.framework.annotation.NotNull;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
public class DictTypeServiceImpl extends ServiceImpl<DictTypeMapper, DictTypeDO>
                                 implements DictTypeService {

    @Autowired
    private DictDataService                dictDataService;

    @Autowired
    private RedisService<String, DictType> redisService;

    @Override
    public long countType(DictType type) {
        if (type == null) {
            return 0;
        }

        return this.baseMapper.countType(BeanUtil.copy(type, DictTypeDO.class));
    }

    @Override
    public List<DictType> listTypes(DictType type) {
        if (type == null) {
            return List.of();
        }

        List<DictType> list = BeanUtil
            .copy(this.baseMapper.listTypes(BeanUtil.copy(type, DictTypeDO.class)), DictType.class);

        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }

        return list;
    }

    @Override
    public DictType getType(String id, String value) {
        if (StringUtils.isBlank(id) && StringUtils.isBlank(value)) {
            return null;
        }

        String key = id + "@" + value;

        DictType type = null;

        try {
            type = redisService.get(RedisService.CACHE_KEY_DICT_TYPE + key);
        } catch (ServiceException e) {
            log.error(RedisService.CACHE_KEY_DICT_TYPE + "{}", key, e);
        }

        if (type != null) {
            return type;
        }

        DictTypeDO typeDO = new DictTypeDO();

        if (StringUtils.isNotBlank(id)) {
            typeDO.setId(new BigInteger(id));
        }

        typeDO.setValue(value);

        var queryWrapper = Wrappers.<DictTypeDO> lambdaQuery()
            .eq(typeDO.getId() != null, DictTypeDO::getId, typeDO.getId())
            .eq(StringUtils.isNotEmpty(value), DictTypeDO::getValue, value);

        type = BeanUtil.copy(this.getOne(queryWrapper), DictType.class);

        if (type == null) {
            return null;
        }

        try {
            redisService.set(RedisService.CACHE_KEY_DICT_TYPE + key, type);
        } catch (ServiceException e) {
            log.error(RedisService.CACHE_KEY_DICT_TYPE + key, e);
        }

        return type;
    }

    @Override
    public DictType getType(BigInteger id, String value) {
        if (id == null && StringUtils.isBlank(value)) {
            return null;
        }

        var queryWrapper = Wrappers.<DictTypeDO> lambdaQuery()
            .eq(id != null, DictTypeDO::getId, id)
            .eq(StringUtils.isNotEmpty(value), DictTypeDO::getValue, value);

        return BeanUtil.copy(this.getOne(queryWrapper), DictType.class);
    }

    @Override
    public DictType insertType(@NotNull DictType type, @NotBlank String creator) {
        DictTypeDO typeDO = BeanUtil.copy(type, DictTypeDO.class);
        typeDO.setCreator(creator);

        this.save(typeDO);

        type.setId(typeDO.getId());

        return type;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public DictType updateType(@NotNull BigInteger id, @NotNull DictType type,
                               @NotBlank String modifier) {
        DictType before = getType(id, null);

        if (before == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "暂无权限");
        }

        type.setId(id);

        DictTypeDO typeDO = BeanUtil.copy(type, DictTypeDO.class);
        typeDO.setModifier(modifier);

        if (!this.updateById(typeDO)) {
            log.error("{}", typeDO);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        dictDataService.updateData(id, type.getValue(), modifier);

        remove(id + "@" + before.getValue());
        remove(id + "@null");
        remove("null@" + before.getValue());

        return type;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public DictType deleteType(@NotNull BigInteger id, @NotBlank String modifier) {
        DictType before = getType(id, null);

        if (before == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "暂无权限");
        }

        DictTypeDO typeDO = new DictTypeDO();
        typeDO.setId(id);
        typeDO.setModifier(modifier);

        var updateWrapper = Wrappers.<DictTypeDO> lambdaUpdate().eq(DictTypeDO::getId, id)
            .set(DictTypeDO::getDeleted, true).set(DictTypeDO::getModifier, modifier);

        if (!this.update(updateWrapper)) {
            log.error("{},{}", id, modifier);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        dictDataService.deleteData(id, null, modifier);

        remove(id + "@" + before.getValue());
        remove(id + "@null");
        remove("null@" + before.getValue());

        return BeanUtil.copy(typeDO, DictType.class);
    }

    private void remove(String key) {
        try {
            redisService.remove(RedisService.CACHE_KEY_DICT_TYPE + key);
        } catch (Exception e) {
            log.error(RedisService.CACHE_KEY_DICT_TYPE + "{}", key, e);
        }
    }

}
