package com.example.demo.dict.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.dict.dao.dataobject.DictTypeDO;

import java.util.List;

public interface DictTypeMapper extends BaseMapper<DictTypeDO> {

    Long count(DictTypeDO dictTypeDO);

    List<DictTypeDO> list(DictTypeDO dictTypeDO);

}
