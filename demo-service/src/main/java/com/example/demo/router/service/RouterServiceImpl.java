package com.example.demo.router.service;

import com.example.demo.menu.api.MenuService;
import com.example.demo.menu.api.bo.Menu;
import com.example.demo.role.api.RoleService;
import com.example.demo.router.api.RouterService;
import com.example.demo.router.api.bo.Meta;
import com.example.demo.router.api.bo.Router;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class RouterServiceImpl implements RouterService {

    @Autowired
    private MenuService menuService;

    @Override
    public List<Router> listRouters(BigInteger userId,
                                    Collection<? extends GrantedAuthority> authorities) {
        if (userId == null) {
            return null;
        }

        boolean isAdmin = false;

        if (authorities != null && !authorities.isEmpty()) {
            for (GrantedAuthority authority : authorities) {
                if (RoleService.ROLE_ADMIN.equals(authority.getAuthority())) {
                    isAdmin = true;
                    break;
                }
            }
        }

        String[] type = new String[] { Menu.Type.CONTENTS.value, Menu.Type.MENU.value };

        List<Menu> menuList = isAdmin ? menuService.listMenus(type)
            : menuService.listMenus(type, userId);

        if (menuList == null || menuList.isEmpty()) {
            return null;
        }

        return getRouterList(BigInteger.ZERO, menuList);
    }

    private List<Router> getRouterList(BigInteger pid, List<Menu> menuList) {
        List<Router> list = new ArrayList<>();

        for (Menu menu : menuList) {
            boolean root = BigInteger.ZERO.compareTo(menu.getPid()) == 0;

            if (pid.compareTo(menu.getPid()) == 0) {
                Router router = getRouter(menu);

                if (!menu.getLeaf()) {
                    router.setAlwaysShow(true);
                    router.setRedirect("noRedirect");
                    router.setChildren(getRouterList(menu.getId(), menuList));
                }

                if (root && Menu.Type.MENU.value.equals(menu.getType()) && !menu.getExternal()) {
                    router.setMeta(null);

                    Router child = getRouter(menu);
                    child.setPath(menu.getPath());
                    child.setName(getName(menu.getComponent()));
                    child.setComponent(menu.getComponent());

                    List<Router> children = new ArrayList<>();
                    children.add(child);

                    router.setChildren(children);
                }

                list.add(router);
            }
        }

        return list;
    }

    private Router getRouter(Menu menu) {
        Router router = new Router();
        router.setHidden(menu.getHidden());
        router.setPath(getPath(menu));
        router.setName(getName(menu));
        router.setComponent(getComponent(menu));
        router.setQuery(menu.getQuery());

        Meta meta = new Meta();
        meta.setNoCache(!menu.getCached());
        meta.setTitle(menu.getName());
        meta.setIcon(menu.getIcon());
        meta.setLink(menu.getPath());

        router.setMeta(meta);

        return router;
    }

    private String getPath(Menu menu) {
        boolean root = BigInteger.ZERO.compareTo(menu.getPid()) == 0;
        String path = menu.getPath();

        if (!root && !menu.getExternal()
            && Strings.CS.startsWithAny(path, "http://", "https://")) {
            return StringUtils.replaceEach(path, new String[] { "http://", "https://" },
                new String[] { "", "" });
        }

        if (root && !menu.getLeaf()) {
            return "/" + path;
        }

        if (root && Menu.Type.MENU.value.equals(menu.getType()) && !menu.getExternal()) {
            return "/";
        }

        if (root && !menu.getExternal() && Strings.CS.startsWithAny(path, "http://", "https://")) {
            return "/";
        }

        return path;
    }

    private String getName(Menu menu) {
        boolean root = BigInteger.ZERO.compareTo(menu.getPid()) == 0;

        if (root && Menu.Type.MENU.value.equals(menu.getType()) && !menu.getExternal()) {
            return "";
        }

        if (StringUtils.isBlank(menu.getComponent())) {
            return StringUtils.capitalize(menu.getPath());
        } else {
            return getName(menu.getComponent());
        }
    }

    private String getName(String component) {
        StringBuilder name = new StringBuilder();

        for (String c : component.split("/")) {
            name.append(StringUtils.capitalize(c));
        }

        return name.toString();
    }

    private String getComponent(Menu menu) {
        boolean root = BigInteger.ZERO.compareTo(menu.getPid()) == 0;

        if (StringUtils.isNotBlank(menu.getComponent())
            && !(root && Menu.Type.MENU.value.equals(menu.getType()) && !menu.getExternal())) {
            return menu.getComponent();
        }

        if (StringUtils.isBlank(menu.getComponent()) && !root && !menu.getExternal()
            && Strings.CS.startsWithAny(menu.getPath(), "http://", "https://")) {
            return "InnerLink";
        }

        if (StringUtils.isBlank(menu.getComponent()) && !root
            && Menu.Type.CONTENTS.value.equals(menu.getType())) {
            return "ParentView";
        }

        return "Layout";
    }

}
