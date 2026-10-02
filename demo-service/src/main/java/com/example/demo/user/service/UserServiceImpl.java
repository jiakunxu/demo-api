package com.example.demo.user.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.framework.annotation.NotBlank;
import com.example.demo.framework.annotation.NotNull;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.security.api.PermissionService;
import com.example.demo.security.api.RefreshTokenService;
import com.example.demo.security.api.bo.LoginUser;
import com.example.demo.user.api.UserService;
import com.example.demo.user.api.bo.User;
import com.example.demo.user.dao.dataobject.UserDO;
import com.example.demo.user.dao.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author JiakunXu
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Autowired
    private PermissionService   permissionService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Override
    public void validate(BigInteger corpId, String id) {
        if (getUser(corpId, id) == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "暂无权限");
        }
    }

    @Override
    public void validate(BigInteger corpId, BigInteger id) {
        if (getUser(corpId, id) == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "暂无权限");
        }
    }

    @Override
    public User getUser(BigInteger id) {
        if (id == null) {
            return null;
        }

        return BeanUtil.copy(this.getById(id), User.class);
    }

    @Override
    public User getUser(BigInteger corpId, String id) {
        if (corpId == null || StringUtils.isBlank(id)) {
            return null;
        }

        return getUser(corpId, new BigInteger(id));
    }

    @Override
    public User getUser(BigInteger corpId, BigInteger id) {
        if (corpId == null || id == null) {
            return null;
        }

        var queryWrapper = Wrappers.<UserDO> lambdaQuery().eq(UserDO::getId, id)
            .eq(UserDO::getCorpId, corpId);

        return BeanUtil.copy(this.getOne(queryWrapper), User.class);
    }

    @Override
    public LoginUser getUser(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }

        UserDO userDO = this
            .getOne(Wrappers.<UserDO> lambdaQuery().eq(UserDO::getUsername, username));

        if (userDO == null) {
            return null;
        }

        LoginUser user = BeanUtil.copy(userDO, LoginUser.class);
        user.setEnabled(Boolean.TRUE.equals(userDO.getEnabled()));
        user.setAccountNonExpired(Boolean.FALSE.equals(userDO.getExpired()));
        user.setAccountNonLocked(Boolean.FALSE.equals(userDO.getLocked()));

        if (user.isEnabled()) {
            user.setAuthorities(permissionService.listPermissions(user.getId()));
        }

        return user;
    }

    @Override
    public User refreshToken(@NotNull BigInteger corpId, @NotNull BigInteger id,
                             @NotBlank String modifier) {
        User before = getUser(corpId, id);

        if (before == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "暂无权限");
        }

        UserDO userDO = new UserDO();
        userDO.setId(id);
        userDO.setCorpId(corpId);
        userDO.setModifier(modifier);
        userDO.setRefreshToken(UUID.randomUUID().toString());

        var updateWrapper = Wrappers.<UserDO> lambdaUpdate().eq(UserDO::getId, id)
            .eq(UserDO::getCorpId, corpId).set(UserDO::getRefreshToken, userDO.getRefreshToken())
            .set(UserDO::getModifier, modifier);

        if (!this.update(updateWrapper)) {
            log.error("{}", userDO);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        refreshTokenService.remove(before);

        return BeanUtil.copy(userDO, User.class);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public List<User> refreshToken(@NotNull BigInteger corpId, @NotNull BigInteger[] ids,
                                   @NotBlank String modifier) {
        List<User> list = new ArrayList<>();

        for (BigInteger id : ids) {
            list.add(refreshToken(corpId, id, modifier));
        }

        return list;
    }

}
