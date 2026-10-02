package com.example.demo.user.api;

import com.example.demo.role.api.bo.Role;
import com.example.demo.user.api.bo.User;
import com.example.demo.user.api.bo.UserRole;

import java.math.BigInteger;
import java.util.List;

public interface UserRoleService {

    long countUserRole(BigInteger roleId);

    long countUserRole(BigInteger userId, String roleCode);

    long countUserRole(BigInteger userId, String... roleCode);

    List<UserRole> listUserRoles(String userId);

    List<UserRole> listUserRoles(BigInteger userId);

    List<Role> listRoles(BigInteger userId, String status);

    long countUser(BigInteger corpId, String roleId, String exists, User user);

    List<User> listUsers(BigInteger corpId, String roleId, String exists, User user);

    List<User> listUsers(BigInteger corpId, String... roleCode);

    UserRole insertUserRole(BigInteger userId, BigInteger roleId, String creator);

    List<UserRole> updateUserRole(BigInteger corpId, BigInteger userId, String[] roleIds,
                                  String modifier);

    List<UserRole> updateUserRole(BigInteger corpId, String[] userIds, BigInteger roleId,
                                  String modifier);

    UserRole deleteUserRole(BigInteger corpId, BigInteger userId, BigInteger roleId,
                            String modifier);

    UserRole deleteUserRole(BigInteger corpId, String[] userIds, BigInteger roleId,
                            String modifier);

}
