package com.example.demo.role.api;

import com.example.demo.role.api.bo.Role;

import java.math.BigInteger;
import java.util.List;

/**
 * @author JiakunXu
 */
public interface RoleService {

    String ROLE_ADMIN = "ROLE_ADMIN";

    long countRole(Role role);

    List<Role> listRoles(Role role);

    List<Role> listRoles();

    Role getRole(String id);

    Role getRole(BigInteger id);

    BigInteger getRoleId(String code);

    Role insertRole(Role role, String creator);

    Role updateRole(BigInteger id, Role role, String modifier);

    Role updateRole(BigInteger id, String status, String modifier);

    Role deleteRole(BigInteger id, String modifier);

}
