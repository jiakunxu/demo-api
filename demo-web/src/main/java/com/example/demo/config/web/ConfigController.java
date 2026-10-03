package com.example.demo.config.web;

import com.example.demo.config.api.ConfigService;
import com.example.demo.config.api.bo.Config;
import com.example.demo.framework.annotation.Log;
import com.example.demo.framework.response.ListResponse;
import com.example.demo.framework.response.ObjectResponse;
import com.example.demo.framework.web.BaseController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/system/config")
public class ConfigController extends BaseController {

    @Autowired
    private ConfigService configService;

    @PreAuthorize("hasAuthority('config:crud')")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ListResponse<Config> list(HttpServletRequest request, HttpServletResponse response) {
        Config config = this.getParameter(request, new Config());

        config.setName(this.getParameter(request, "name"));
        config.setKey(this.getParameter(request, "key"));

        String system = this.getParameter(request, "system");
        if (StringUtils.isNotBlank(system)) {
            config.setSystem("true".equals(system));
        }

        long count = configService.countConfig(config);

        if (count == 0) {
            return new ListResponse<>(0, null);
        }

        return new ListResponse<>(count, configService.listConfigs(config));
    }

    @PreAuthorize("hasAuthority('config:crud')")
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ObjectResponse<Config> get(HttpServletRequest request, HttpServletResponse response) {
        String id = this.getParameter(request, "id");
        String key = this.getParameter(request, "key");
        return new ObjectResponse<>(configService.getConfig(id, key));
    }

    @Log(module = "")
    @PreAuthorize("hasAuthority('config:crud')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ObjectResponse<Config> save(HttpServletRequest request, HttpServletResponse response) {
        Config config = this.getParameter(request, Config.class);
        return new ObjectResponse<>(configService.insertConfig(config, this.getUser().getName()));
    }

    @Log(module = "")
    @PreAuthorize("hasAuthority('config:crud')")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ObjectResponse<Config> update(HttpServletRequest request, HttpServletResponse response) {
        Config config = this.getParameter(request, Config.class);
        return new ObjectResponse<>(
            configService.updateConfig(config.getId(), config, this.getUser().getName()));
    }

    @Log(module = "")
    @PreAuthorize("hasAuthority('config:crud')")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ObjectResponse<Config> delete(HttpServletRequest request, HttpServletResponse response) {
        Config config = this.getParameter(request, Config.class);
        return new ObjectResponse<>(
            configService.deleteConfig(config.getId(), this.getUser().getName()));
    }

}
