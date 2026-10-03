package com.example.demo.user.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.security.api.PermissionService;
import com.example.demo.security.api.bo.LoginUser;
import com.example.demo.user.api.UserLoginService;
import com.example.demo.user.dao.dataobject.UserDO;
import com.example.demo.user.dao.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Slf4j
@Service
public class UserLoginServiceImpl extends ServiceImpl<UserMapper, UserDO>
                                  implements UserLoginService {

    @Autowired
    private PermissionService permissionService;

    @Override
    public LoginUser getUser(BigInteger id) {
        if (id == null) {
            return null;
        }

        return setAuthorities(this.getById(id));
    }

    @Override
    public LoginUser getUser(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }

        UserDO userDO = this
            .getOne(Wrappers.<UserDO> lambdaQuery().eq(UserDO::getUsername, username));

        return setAuthorities(userDO);
    }

    private LoginUser setAuthorities(UserDO userDO) {
        if (userDO == null) {
            return null;
        }

        LoginUser user = BeanUtil.copy(userDO, LoginUser.class);

        user.setEnabled(Boolean.TRUE.equals(userDO.getEnabled()));
        user.setAccountNonExpired(Boolean.FALSE.equals(userDO.getExpired()));
        user.setAccountNonLocked(Boolean.FALSE.equals(userDO.getLocked()));

        if (!user.isEnabled()) {
            return user;
        }

        user.setAuthorities(permissionService.listPermissions(user.getId()));

        return user;
    }

}
