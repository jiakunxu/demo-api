package com.example.demo.user.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.user.dao.dataobject.UserDO;
import com.example.demo.user.dao.dataobject.UserRoleDO;

import java.util.List;

public interface UserRoleMapper extends BaseMapper<UserRoleDO> {

    int countUserRole(UserRoleDO userRoleDO);

    List<UserRoleDO> listUserRoles(UserRoleDO userRoleDO);

    int countUser(UserDO userDO);

    List<UserDO> listUsers(UserDO userDO);

}
