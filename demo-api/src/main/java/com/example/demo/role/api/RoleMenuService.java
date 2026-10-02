package com.example.demo.role.api;

import com.example.demo.role.api.bo.RoleMenu;

import java.math.BigInteger;
import java.util.List;

public interface RoleMenuService {

    long countRoleMenu(BigInteger menuId);

    List<RoleMenu> listRoleMenus(BigInteger roleId);

    List<BigInteger> listRoleMenus(String roleId);

    List<RoleMenu> updateRoleMenus(BigInteger roleId, BigInteger[] menuIds, String modifier);

    RoleMenu deleteRoleMenu(BigInteger roleId, BigInteger menuId, String modifier);

}
