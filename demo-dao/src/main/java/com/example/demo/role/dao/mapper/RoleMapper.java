package com.example.demo.role.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.role.dao.dataobject.RoleDO;

import java.util.List;

public interface RoleMapper extends BaseMapper<RoleDO> {

    int countRole(RoleDO roleDO);

    List<RoleDO> listRoles(RoleDO roleDO);

}
