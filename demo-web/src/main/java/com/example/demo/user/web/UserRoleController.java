package com.example.demo.user.web;

import com.alibaba.fastjson2.JSONObject;
import com.example.demo.framework.annotation.Log;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.response.ListResponse;
import com.example.demo.framework.response.ObjectResponse;
import com.example.demo.framework.web.BaseController;
import com.example.demo.role.api.RoleService;
import com.example.demo.role.api.bo.Role;
import com.example.demo.user.api.UserRoleService;
import com.example.demo.user.api.UserService;
import com.example.demo.user.api.bo.User;
import com.example.demo.user.api.bo.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/system/user/role")
public class UserRoleController extends BaseController {

    @Autowired
    private RoleService     roleService;

    @Autowired
    private UserService     userService;

    @Autowired
    private UserRoleService userRoleService;

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ObjectResponse<Map<String, Object>> get(HttpServletRequest request,
                                                   HttpServletResponse response) {
        BigInteger corpId = this.getUser().getCorpId();
        String userId = this.getParameter(request, "userId");

        User user = userService.getUser(corpId, userId);

        if (user == null) {
            Map<String, Object> map = new HashMap<>();
            map.put("user", null);
            map.put("roles", null);

            return new ObjectResponse<>(map);
        }

        // TODO
        List<Role> roles = roleService.listRoles();

        if (roles != null && !roles.isEmpty()) {
            List<UserRole> userRoleList = userRoleService.listUserRoles(userId);
            if (userRoleList != null && !userRoleList.isEmpty()) {
                Map<BigInteger, UserRole> map = new HashMap<>();
                for (UserRole userRole : userRoleList) {
                    map.put(userRole.getRoleId(), userRole);
                }
                for (Role role : roles) {
                    if (map.containsKey(role.getId())) {
                        role.setStatus("selected");
                    }
                }
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("roles", roles);

        return new ObjectResponse<>(map);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ListResponse<User> list(HttpServletRequest request, HttpServletResponse response) {
        BigInteger corpId = this.getUser().getCorpId();
        String roleId = this.getParameter(request, "roleId");
        String exists = this.getParameter(request, "exists");
        User user = this.getParameter(request, new User());

        user.setName(this.getParameter(request, "name"));
        user.setMobile(this.getParameter(request, "mobile"));

        long count = userRoleService.countUser(corpId, roleId, exists, user);

        if (count == 0) {
            return new ListResponse<>(0, null);
        }

        return new ListResponse<>(count, userRoleService.listUsers(corpId, roleId, exists, user));
    }

    @Log(module = "")
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ListResponse<UserRole> update(HttpServletRequest request, HttpServletResponse response) {
        User user = this.getUser();
        JSONObject data = this.getParameter(request, JSONObject.class);

        if (data == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "参数信息不能为空");
        }

        BigInteger userId = data.getBigInteger("userId");
        String roleIds = data.getString("roleIds");
        String userIds = data.getString("userIds");
        BigInteger roleId = data.getBigInteger("roleId");

        if (userId != null) {
            return new ListResponse<>(userRoleService.updateUserRole(user.getCorpId(), userId,
                StringUtils.isBlank(roleIds) ? new String[0] : roleIds.split(","), user.getName()));
        } else {
            return new ListResponse<>(userRoleService.updateUserRole(user.getCorpId(),
                StringUtils.isBlank(userIds) ? new String[0] : userIds.split(","), roleId,
                user.getName()));
        }
    }

    @Log(module = "")
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public ObjectResponse<UserRole> remove(HttpServletRequest request,
                                           HttpServletResponse response) {
        User user = this.getUser();
        JSONObject data = this.getParameter(request, JSONObject.class);

        if (data == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "参数信息不能为空");
        }

        BigInteger userId = data.getBigInteger("userId");
        String userIds = data.getString("userIds");
        BigInteger roleId = data.getBigInteger("roleId");

        if (userId != null) {
            return new ObjectResponse<>(
                userRoleService.deleteUserRole(user.getCorpId(), userId, roleId, user.getName()));
        } else {
            return new ObjectResponse<>(userRoleService.deleteUserRole(user.getCorpId(),
                StringUtils.isBlank(userIds) ? new String[0] : userIds.split(","), roleId,
                user.getName()));
        }
    }

}
