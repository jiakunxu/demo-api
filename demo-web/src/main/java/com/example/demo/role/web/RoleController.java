package com.example.demo.role.web;

import com.example.demo.framework.annotation.Log;
import com.example.demo.framework.response.ListResponse;
import com.example.demo.framework.response.ObjectResponse;
import com.example.demo.framework.web.BaseController;
import com.example.demo.role.api.RoleService;
import com.example.demo.role.api.bo.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/system/role")
public class RoleController extends BaseController {

    @Autowired
    private RoleService roleService;

    @PreAuthorize("hasAuthority('role:crud')")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ListResponse<Role> list(HttpServletRequest request, HttpServletResponse response) {
        Role role = this.getParameter(request, new Role());

        role.setCode(this.getParameter(request, "code"));
        role.setName(this.getParameter(request, "name"));
        role.setStatus(this.getParameter(request, "status"));

        long count = roleService.countRole(role);

        if (count == 0) {
            return new ListResponse<>(0, null);
        }

        return new ListResponse<>(count, roleService.listRoles(role));
    }

    @PreAuthorize("hasAuthority('role:crud')")
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ObjectResponse<Role> get(HttpServletRequest request, HttpServletResponse response) {
        String id = this.getParameter(request, "id");
        return new ObjectResponse<>(roleService.getRole(id));
    }

    @Log(module = "")
    @PreAuthorize("hasAuthority('role:crud')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ObjectResponse<Role> save(HttpServletRequest request, HttpServletResponse response) {
        Role role = this.getParameter(request, Role.class);
        return new ObjectResponse<>(roleService.insertRole(role, this.getUser().getName()));
    }

    @Log(module = "")
    @PreAuthorize("hasAuthority('role:crud')")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ObjectResponse<Role> update(HttpServletRequest request, HttpServletResponse response) {
        Role role = this.getParameter(request, Role.class);
        return new ObjectResponse<>(
            roleService.updateRole(role.getId(), role, this.getUser().getName()));
    }

    @Log(module = "")
    @PreAuthorize("hasAuthority('role:crud')")
    @RequestMapping(value = "/status/update", method = RequestMethod.POST)
    public ObjectResponse<Role> updateStatus(HttpServletRequest request,
                                             HttpServletResponse response) {
        Role role = this.getParameter(request, Role.class);
        return new ObjectResponse<>(
            roleService.updateRole(role.getId(), role.getStatus(), this.getUser().getName()));
    }

    @Log(module = "")
    @PreAuthorize("hasAuthority('role:crud')")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ObjectResponse<Role> delete(HttpServletRequest request, HttpServletResponse response) {
        Role role = this.getParameter(request, Role.class);
        return new ObjectResponse<>(roleService.deleteRole(role.getId(), this.getUser().getName()));
    }

}
