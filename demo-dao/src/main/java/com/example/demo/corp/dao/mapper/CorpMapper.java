package com.example.demo.corp.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.corp.dao.dataobject.CorpDO;

import java.util.List;

/**
 * @author JiakunXu
 */
public interface CorpMapper extends BaseMapper<CorpDO> {

    long countCorp(CorpDO corpDO);

    List<CorpDO> listCorps(CorpDO corpDO);

}
