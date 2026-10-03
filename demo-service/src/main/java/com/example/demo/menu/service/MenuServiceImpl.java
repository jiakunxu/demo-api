package com.example.demo.menu.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.demo.framework.annotation.NotBlank;
import com.example.demo.framework.annotation.NotNull;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.menu.api.MenuService;
import com.example.demo.menu.api.bo.Menu;
import com.example.demo.menu.dao.dataobject.MenuDO;
import com.example.demo.menu.dao.mapper.MenuMapper;
import com.example.demo.role.api.RoleMenuService;
import com.example.demo.tree.api.bo.Tree;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, MenuDO> implements MenuService {

    @Autowired
    private RoleMenuService roleMenuService;

    @Override
    public long countMenu(BigInteger pid) {
        if (pid == null) {
            return 0;
        }

        MenuDO menuDO = new MenuDO();
        menuDO.setPid(pid);

        return this.baseMapper.countMenu(menuDO);
    }

    @Override
    public long countMenu(String pid, Menu menu) {
        if (menu == null) {
            return 0;
        }

        if (StringUtils.isNotBlank(pid)) {
            menu.setPid(new BigInteger(pid));
        }

        return this.baseMapper.countMenu(BeanUtil.copy(menu, MenuDO.class));
    }

    @Override
    public List<Menu> listMenus(String pid, Menu menu) {
        if (menu == null) {
            return null;
        }

        if (StringUtils.isNotBlank(pid)) {
            menu.setPid(new BigInteger(pid));
        }

        return BeanUtil.copy(this.baseMapper.listMenus(BeanUtil.copy(menu, MenuDO.class)),
            Menu.class);
    }

    @Override
    public List<Menu> listMenus(String name, String status) {
        MenuDO menuDO = new MenuDO();
        menuDO.setName(name);
        menuDO.setStatus(status);

        return BeanUtil.copy(this.baseMapper.listMenus(menuDO), Menu.class);
    }

    @Override
    public List<Tree> listMenus() {
        return listMenus((List<BigInteger>) null);
    }

    @Override
    public List<Tree> listMenus(List<BigInteger> ids) {
        MenuDO menuDO = new MenuDO();
        menuDO.setIds(ids);

        List<Menu> menus = BeanUtil.copy(this.baseMapper.listMenus(menuDO), Menu.class);

        if (CollectionUtils.isEmpty(menus)) {
            return null;
        }

        return getTreeList(BigInteger.ZERO, menus);
    }

    private List<Tree> getTreeList(BigInteger pid, List<Menu> menus) {
        List<Tree> list = new ArrayList<>();

        for (Menu menu : menus) {
            if (pid.compareTo(menu.getPid()) == 0) {
                Tree tree = new Tree(menu.getId(), menu.getName());

                tree.setChildren(getTreeList(menu.getId(), menus));

                list.add(tree);
            }
        }

        return list;
    }

    @Override
    public List<Menu> listMenus(String[] type) {
        if (type == null || type.length == 0) {
            return null;
        }

        MenuDO menuDO = new MenuDO();
        menuDO.setStatus(Menu.Status.ENABLE.value);
        menuDO.setTypes(type);

        return BeanUtil.copy(this.baseMapper.listMenus(menuDO), Menu.class);
    }

    @Override
    public List<Menu> listMenus(String[] type, BigInteger userId) {
        if (type == null || type.length == 0 || userId == null) {
            return null;
        }

        MenuDO menuDO = new MenuDO();
        menuDO.setStatus(Menu.Status.ENABLE.value);
        menuDO.setTypes(type);
        menuDO.setUserId(userId);

        return BeanUtil.copy(this.baseMapper.listMenus(menuDO), Menu.class);
    }

    @Override
    public Menu getMenu(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }

        return getMenu(new BigInteger(id));
    }

    @Override
    public Menu getMenu(BigInteger id) {
        if (id == null) {
            return null;
        }

        return BeanUtil.copy(this.getById(id), Menu.class);
    }

    @Override
    public Menu insertMenu(@NotNull BigInteger pid, @NotNull Menu menu, @NotBlank String creator) {
        menu.setPid(pid);

        MenuDO menuDO = BeanUtil.copy(menu, MenuDO.class);
        menuDO.setCreator(creator);

        this.save(menuDO);

        menu.setId(menuDO.getId());

        return menu;
    }

    @Override
    public Menu updateMenu(@NotNull BigInteger id, @NotNull Menu menu, @NotBlank String modifier) {
        menu.setId(id);

        MenuDO menuDO = BeanUtil.copy(menu, MenuDO.class);
        menuDO.setModifier(modifier);

        if (!this.updateById(menuDO)) {
            log.error("{}", menuDO);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        return menu;
    }

    @Override
    public Menu deleteMenu(@NotNull BigInteger id, @NotBlank String modifier) {
        if (countMenu(id) > 0) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "已包含菜单，请先删除下级菜单");
        }

        if (roleMenuService.countRoleMenu(id) > 0) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "已关联角色，请先调整角色菜单");
        }

        var updateWrapper = Wrappers.<MenuDO> lambdaUpdate().eq(MenuDO::getId, id)
            .set(MenuDO::getDeleted, true).set(MenuDO::getModifier, modifier);

        if (!this.update(updateWrapper)) {
            log.error("{},{}", id, modifier);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        return new Menu(id);
    }

}
