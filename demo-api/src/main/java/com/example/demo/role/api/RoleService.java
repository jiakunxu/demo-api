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

    List<Role> listRoles();

    List<Role> listRoles(Role role);

    List<Role> listRoles(List<BigInteger> ids);

    Role getRole(String id);

    Role getRole(BigInteger id);

    BigInteger getRoleId(String code);

    Role insertRole(Role role, String createBy);

    Role updateRole(BigInteger id, Role role, String updateBy);

    Role updateRole(BigInteger id, String status, String updateBy);

    Role deleteRole(BigInteger id, String updateBy);

}
