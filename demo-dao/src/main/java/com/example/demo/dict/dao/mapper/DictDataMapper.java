package com.example.demo.dict.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.dict.dao.dataobject.DictDataDO;

import java.util.List;

public interface DictDataMapper extends BaseMapper<DictDataDO> {

    Long count(DictDataDO dictDataDO);

    List<DictDataDO> list(DictDataDO dictDataDO);

}
