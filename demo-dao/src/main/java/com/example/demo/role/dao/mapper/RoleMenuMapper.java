package com.example.demo.role.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.role.dao.dataobject.RoleMenuDO;

import java.util.List;

public interface RoleMenuMapper extends BaseMapper<RoleMenuDO> {

    long countRoleMenu(RoleMenuDO roleMenuDO);

    List<RoleMenuDO> listRoleMenus(RoleMenuDO roleMenuDO);

}
