package com.example.demo.role.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.cache.api.RedisService;
import com.example.demo.framework.annotation.NotBlank;
import com.example.demo.framework.annotation.NotNull;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.role.api.RoleMenuService;
import com.example.demo.role.api.RoleService;
import com.example.demo.role.api.bo.Role;
import com.example.demo.role.dao.dataobject.RoleDO;
import com.example.demo.role.dao.mapper.RoleMapper;
import com.example.demo.user.api.UserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, RoleDO> implements RoleService {

    @Autowired
    private RedisService<String, Role> redisService;

    @Autowired
    private RoleMenuService            roleMenuService;

    @Autowired
    private UserRoleService            userRoleService;

    @Override
    public long countRole(Role role) {
        if (role == null) {
            return 0;
        }

        return this.baseMapper.countRole(BeanUtil.copy(role, RoleDO.class));
    }

    @Override
    public List<Role> listRoles() {
        Role role = new Role();
        role.setPageNo(1);
        role.setPageSize(99);

        return listRoles(role);
    }

    @Override
    public List<Role> listRoles(Role role) {
        if (role == null) {
            return List.of();
        }

        List<Role> list = BeanUtil
            .copy(this.baseMapper.listRoles(BeanUtil.copy(role, RoleDO.class)), Role.class);

        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }

        return list;
    }

    @Override
    public Role getRole(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }

        return BeanUtil.copy(this.getById(new BigInteger(id)), Role.class);
    }

    @Override
    public Role getRole(@NotNull BigInteger id) {
        String key = RedisService.CACHE_KEY_ROLE + id;

        Role role = null;

        try {
            role = redisService.get(key);
        } catch (ServiceException e) {
            log.error(RedisService.CACHE_KEY_ROLE + "{}", key, e);
        }

        if (role != null) {
            return role;
        }

        role = BeanUtil.copy(this.getById(id), Role.class);

        if (role == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "角色不存在");
        }

        try {
            redisService.set(key, role);
        } catch (ServiceException e) {
            log.error("{}", key, e);
        }

        return role;
    }

    @Override
    public BigInteger getRoleId(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }

        RoleDO role = this.getOne(Wrappers.<RoleDO> lambdaQuery().eq(RoleDO::getCode, code));

        if (role == null) {
            return null;
        }

        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Role insertRole(@NotNull Role role, @NotBlank String creator) {
        RoleDO roleDO = BeanUtil.copy(role, RoleDO.class);
        roleDO.setCreator(creator);

        try {
            this.save(roleDO);
        } catch (DuplicateKeyException e) {
            log.error("{}", roleDO, e);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "编号已存在");
        }

        role.setId(roleDO.getId());

        roleMenuService.updateRoleMenus(role.getId(), role.getMenuIds(), creator);

        return role;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Role updateRole(@NotNull BigInteger id, @NotNull Role role, @NotBlank String modifier) {
        role.setId(id);

        RoleDO roleDO = BeanUtil.copy(role, RoleDO.class);
        roleDO.setModifier(modifier);

        if (!this.updateById(roleDO)) {
            log.error("{}", roleDO);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        roleMenuService.updateRoleMenus(role.getId(), role.getMenuIds(), modifier);

        remove(id);

        return role;
    }

    @Override
    public Role updateRole(@NotNull BigInteger id, @NotBlank String status,
                           @NotBlank String modifier) {
        RoleDO roleDO = new RoleDO();
        roleDO.setId(id);
        roleDO.setStatus(status);
        roleDO.setModifier(modifier);

        if (!this.updateById(roleDO)) {
            log.error("{}", roleDO);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        remove(id);

        return BeanUtil.copy(roleDO, Role.class);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Role deleteRole(@NotNull BigInteger id, @NotBlank String modifier) {
        if (userRoleService.countUserRole(id) > 0) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "已关联用户，请先调整用户角色");
        }

        RoleDO roleDO = new RoleDO();
        roleDO.setId(id);
        roleDO.setModifier(modifier);

        var updateWrapper = Wrappers.<RoleDO> lambdaUpdate().eq(RoleDO::getId, id)
            .set(RoleDO::getDeleted, true).set(RoleDO::getModifier, modifier);

        if (!this.update(updateWrapper)) {
            log.error("{}", roleDO);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        if (CollectionUtils.isNotEmpty(roleMenuService.listRoleMenus(id))) {
            roleMenuService.deleteRoleMenu(id, null, modifier);
        }

        remove(id);

        return BeanUtil.copy(roleDO, Role.class);
    }

    private void remove(BigInteger id) {
        String key = RedisService.CACHE_KEY_ROLE + id;

        try {
            redisService.remove(key);
        } catch (Exception e) {
            log.error("{}", key, e);
        }
    }

}
