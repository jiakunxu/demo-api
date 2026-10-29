package com.example.demo.role.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.role.dao.dataobject.RoleMenuDO;

import java.util.List;

public interface RoleMenuMapper extends BaseMapper<RoleMenuDO> {

    Long count(RoleMenuDO roleMenuDO);

    List<RoleMenuDO> list(RoleMenuDO roleMenuDO);

}
