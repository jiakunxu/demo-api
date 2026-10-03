package com.example.demo.menu.api;

import com.example.demo.menu.api.bo.Menu;
import com.example.demo.tree.api.bo.Tree;

import java.math.BigInteger;
import java.util.List;

/**
 * @author JiakunXu
 */
public interface MenuService {

    long countMenu(BigInteger pid);

    long countMenu(String pid, Menu menu);

    List<Menu> listMenus(String pid, Menu menu);

    List<Menu> listMenus(String name, String status);

    List<Tree> listMenus();

    List<Tree> listMenus(List<BigInteger> ids);

    List<Menu> listMenus(String[] type);

    List<Menu> listMenus(String[] type, BigInteger userId);

    Menu getMenu(String id);

    Menu getMenu(BigInteger id);

    Menu insertMenu(BigInteger pid, Menu menu, String creator);

    Menu updateMenu(BigInteger id, Menu menu, String modifier);

    Menu deleteMenu(BigInteger id, String modifier);

}
