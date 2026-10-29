package com.example.demo.user.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.user.dao.dataobject.UserDO;
import com.example.demo.user.dao.dataobject.UserRoleDO;

import java.util.List;

public interface UserRoleMapper extends BaseMapper<UserRoleDO> {

    Long count(UserRoleDO userRoleDO);

    List<UserRoleDO> list(UserRoleDO userRoleDO);

    Long countUser(UserDO userDO);

    List<UserDO> listUsers(UserDO userDO);

}
