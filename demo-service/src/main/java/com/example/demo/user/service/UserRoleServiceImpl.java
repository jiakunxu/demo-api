package com.example.demo.user.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.demo.framework.annotation.NotBlank;
import com.example.demo.framework.annotation.NotNull;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.role.api.RoleService;
import com.example.demo.role.api.bo.Role;
import com.example.demo.user.api.UserRoleService;
import com.example.demo.user.api.UserService;
import com.example.demo.user.api.bo.User;
import com.example.demo.user.api.bo.UserRole;
import com.example.demo.user.dao.dataobject.UserDO;
import com.example.demo.user.dao.dataobject.UserRoleDO;
import com.example.demo.user.dao.mapper.UserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRoleDO>
                                 implements UserRoleService {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    @Override
    public long countUserRole(BigInteger roleId) {
        if (roleId == null) {
            return 0;
        }

        UserRoleDO userRoleDO = new UserRoleDO();
        userRoleDO.setRoleId(roleId);

        return this.baseMapper.countUserRole(userRoleDO);
    }

    @Override
    public long countUserRole(BigInteger userId, String roleCode) {
        if (userId == null || StringUtils.isBlank(roleCode)) {
            return 0;
        }

        UserRoleDO userRoleDO = new UserRoleDO();
        userRoleDO.setUserId(userId);
        userRoleDO.setCode(roleCode);

        return this.baseMapper.countUserRole(userRoleDO);
    }

    @Override
    public long countUserRole(BigInteger userId, String... roleCode) {
        if (userId == null || roleCode == null || roleCode.length == 0) {
            return 0;
        }

        UserRoleDO userRoleDO = new UserRoleDO();
        userRoleDO.setUserId(userId);
        userRoleDO.setCodes(roleCode);

        return this.baseMapper.countUserRole(userRoleDO);
    }

    @Override
    public List<UserRole> listUserRoles(String userId) {
        if (StringUtils.isBlank(userId)) {
            return List.of();
        }

        return listUserRoles(new BigInteger(userId));
    }

    @Override
    public List<UserRole> listUserRoles(BigInteger userId) {
        if (userId == null) {
            return List.of();
        }

        UserRoleDO userRoleDO = new UserRoleDO();
        userRoleDO.setUserId(userId);

        List<UserRole> list = BeanUtil.copy(this.baseMapper.listUserRoles(userRoleDO),
            UserRole.class);

        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }

        return list;
    }

    @Override
    public List<Role> listRoles(BigInteger userId, String status) {
        if (userId == null) {
            return List.of();
        }

        UserRoleDO userRoleDO = new UserRoleDO();
        userRoleDO.setUserId(userId);
        userRoleDO.setStatus(status);

        List<UserRole> userRoleList = BeanUtil.copy(this.baseMapper.listUserRoles(userRoleDO),
            UserRole.class);

        if (CollectionUtils.isEmpty(userRoleList)) {
            return List.of();
        }

        List<Role> list = new ArrayList<>();

        for (UserRole userRole : userRoleList) {
            list.add(roleService.getRole(userRole.getRoleId()));
        }

        return list;
    }

    @Override
    public long countUser(BigInteger corpId, String roleId, String exists, User user) {
        if (corpId == null || StringUtils.isBlank(roleId) || user == null) {
            return 0;
        }

        // TODO roleService.validate(corpId, roleId);

        user.setCorpId(corpId);

        UserDO userDO = BeanUtil.copy(user, UserDO.class);
        userDO.setRoleId(new BigInteger(roleId));
        userDO.setExists("true".equals(exists));

        return this.baseMapper.countUser(userDO);
    }

    @Override
    public List<User> listUsers(BigInteger corpId, String roleId, String exists, User user) {
        if (corpId == null || StringUtils.isBlank(roleId) || user == null) {
            return List.of();
        }

        user.setCorpId(corpId);

        UserDO userDO = BeanUtil.copy(user, UserDO.class);
        userDO.setRoleId(new BigInteger(roleId));
        userDO.setExists("true".equals(exists));

        List<User> list = BeanUtil.copy(this.baseMapper.listUsers(userDO), User.class);

        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }

        return list;
    }

    @Override
    public List<User> listUsers(BigInteger corpId, String... roleCode) {
        if (corpId == null || roleCode == null || roleCode.length == 0) {
            return List.of();
        }

        UserDO userDO = new UserDO();
        userDO.setCorpId(corpId);
        userDO.setEnabled(Boolean.TRUE);
        userDO.setExists(Boolean.TRUE);
        userDO.setCodes(roleCode);

        long count = this.baseMapper.countUser(userDO);

        if (count == 0) {
            return List.of();
        }

        userDO.setPageNo(1L);
        userDO.setPageSize(count);

        List<User> list = BeanUtil.copy(this.baseMapper.listUsers(userDO), User.class);

        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }

        return list;
    }

    @Override
    public UserRole insertUserRole(@NotNull BigInteger userId, @NotNull BigInteger roleId,
                                   @NotBlank String creator) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);

        UserRoleDO userRoleDO = BeanUtil.copy(userRole, UserRoleDO.class);
        userRoleDO.setCreator(creator);

        this.save(userRoleDO);

        userRole.setId(userRoleDO.getId());

        return userRole;
    }

    private void insertUserRole(BigInteger userId, List<UserRole> userRoleList, String creator) {
        if (CollectionUtils.isEmpty(userRoleList)) {
            return;
        }

        List<UserRoleDO> userRoleDOs = new ArrayList<>(userRoleList.size());

        for (UserRole userRole : userRoleList) {
            userRole.setUserId(userId);

            UserRoleDO userRoleDO = BeanUtil.copy(userRole, UserRoleDO.class);
            userRoleDO.setCreator(creator);

            userRoleDOs.add(userRoleDO);
        }

        this.saveBatch(userRoleDOs);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public List<UserRole> updateUserRole(@NotNull BigInteger corpId, BigInteger userId,
                                         String[] roleIds, @NotBlank String modifier) {
        userService.validate(corpId, userId);

        List<UserRole> userRoleList = new ArrayList<>();

        if (roleIds != null && roleIds.length > 0) {
            for (String roleId : roleIds) {
                // TODO roleService.validate(corpId, roleId);

                UserRole userRole = new UserRole();
                userRole.setRoleId(new BigInteger(roleId));

                userRoleList.add(userRole);
            }
        }

        List<UserRole> list0 = listUserRoles(userId);

        if (CollectionUtils.isEmpty(list0)) {
            insertUserRole(userId, userRoleList, modifier);

            userService.refreshToken(corpId, userId, modifier);

            return userRoleList;
        }

        Map<BigInteger, UserRole> map = new HashMap<>(list0.size());

        for (UserRole userRole : list0) {
            map.put(userRole.getRoleId(), userRole);
        }

        List<UserRole> list1 = new ArrayList<>();

        for (UserRole userRole : userRoleList) {
            if (map.containsKey(userRole.getRoleId())) {
                map.remove(userRole.getRoleId());
            } else {
                list1.add(userRole);
            }
        }

        insertUserRole(userId, list1, modifier);

        for (Map.Entry<BigInteger, UserRole> m : map.entrySet()) {
            UserRoleDO userRoleDO = BeanUtil.copy(m.getValue(), UserRoleDO.class);
            userRoleDO.setModifier(modifier);

            var updateWrapper = Wrappers.<UserRoleDO> lambdaUpdate()
                .eq(UserRoleDO::getId, userRoleDO.getId()).set(UserRoleDO::getDeleted, true)
                .set(UserRoleDO::getModifier, modifier);

            if (!this.update(updateWrapper)) {
                log.error("{}", userRoleDO);
                throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
            }
        }

        userService.refreshToken(corpId, userId, modifier);

        return userRoleList;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public List<UserRole> updateUserRole(@NotNull BigInteger corpId, @NotNull String[] userIds,
                                         BigInteger roleId, @NotBlank String modifier) {
        // TODO roleService.validate(corpId, roleId);

        if (userIds.length == 0) {
            return List.of();
        }

        List<UserRoleDO> userRoleDOs = new ArrayList<>(userIds.length);

        for (String userId : userIds) {
            userService.validate(corpId, userId);

            UserRoleDO userRoleDO = new UserRoleDO();
            userRoleDO.setUserId(new BigInteger(userId));
            userRoleDO.setRoleId(roleId);
            userRoleDO.setCreator(modifier);

            userRoleDOs.add(userRoleDO);
        }

        this.saveBatch(userRoleDOs);

        for (UserRoleDO userRoleDO : userRoleDOs) {
            userService.refreshToken(corpId, userRoleDO.getUserId(), modifier);
        }

        return BeanUtil.copy(userRoleDOs, UserRole.class);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public UserRole deleteUserRole(@NotNull BigInteger corpId, BigInteger userId, BigInteger roleId,
                                   @NotBlank String modifier) {
        userService.validate(corpId, userId);
        // TODO roleService.validate(corpId, roleId);

        UserRoleDO userRoleDO = new UserRoleDO();
        userRoleDO.setUserId(userId);
        userRoleDO.setRoleId(roleId);
        userRoleDO.setModifier(modifier);

        var updateWrapper = Wrappers.<UserRoleDO> lambdaUpdate().eq(UserRoleDO::getUserId, userId)
            .eq(UserRoleDO::getRoleId, roleId).set(UserRoleDO::getDeleted, true)
            .set(UserRoleDO::getModifier, modifier);

        if (!this.update(updateWrapper)) {
            log.error("{}", userRoleDO);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        userService.refreshToken(corpId, userId, modifier);

        return BeanUtil.copy(userRoleDO, UserRole.class);
    }

    @Override
    public UserRole deleteUserRole(@NotNull BigInteger corpId, @NotNull String[] userIds,
                                   BigInteger roleId, @NotBlank String modifier) {
        // TODO roleService.validate(corpId, roleId);

        if (userIds.length == 0) {
            UserRole userRole = new UserRole();
            userRole.setRoleId(roleId);
            return userRole;
        }

        List<BigInteger> list = new ArrayList<>();

        for (String userId : userIds) {
            userService.validate(corpId, userId);

            list.add(new BigInteger(userId));
        }

        UserRoleDO userRoleDO = new UserRoleDO();
        userRoleDO.setRoleId(roleId);
        userRoleDO.setUserIds(list.toArray(new BigInteger[0]));
        userRoleDO.setModifier(modifier);

        var updateWrapper = Wrappers.<UserRoleDO> lambdaUpdate().in(UserRoleDO::getUserId, list)
            .eq(UserRoleDO::getRoleId, roleId).set(UserRoleDO::getDeleted, true)
            .set(UserRoleDO::getModifier, modifier);

        if (!this.update(updateWrapper)) {
            log.error("{}", userRoleDO);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        userService.refreshToken(corpId, list.toArray(new BigInteger[0]), modifier);

        return BeanUtil.copy(userRoleDO, UserRole.class);
    }

}
