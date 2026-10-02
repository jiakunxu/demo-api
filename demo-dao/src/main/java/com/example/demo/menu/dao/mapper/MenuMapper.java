package com.example.demo.menu.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.menu.dao.dataobject.MenuDO;

import java.util.List;

public interface MenuMapper extends BaseMapper<MenuDO> {

    int countMenu(MenuDO menuDO);

    List<MenuDO> listMenus(MenuDO menuDO);

}
