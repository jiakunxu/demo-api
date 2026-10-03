package com.example.demo.framework.web;

import com.alibaba.fastjson2.JSON;
import com.example.demo.framework.bo.BaseBO;
import com.example.demo.security.api.bo.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author JiakunXu
 */
@Slf4j
@RestController
public class BaseController {

    public LoginUser getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "anonymousUser".equals(authentication.getName()) ? new LoginUser()
            : (LoginUser) authentication.getPrincipal();
    }

    public String getParameter(HttpServletRequest request) {
        try {
            return IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("", e);
        }

        return null;
    }

    public String getParameter(HttpServletRequest request, String name) {
        return request.getParameter(name);
    }

    public <T extends BaseBO> T getParameter(HttpServletRequest request, T parameter) {
        ServletRequestDataBinder binder = new ServletRequestDataBinder(parameter);
        binder.bind(request);

        parameter.setSearch(request.getParameter("search"));
        parameter.setStart(request.getParameter("startDate"));
        parameter.setEnd(request.getParameter("endDate"));
        parameter.setSort(request.getParameter("sort"));

        String dir = request.getParameter("dir");
        if ("ascending".equals(dir)) {
            parameter.setDir("ASC");
        } else if ("descending".equals(dir)) {
            parameter.setDir("DESC");
        } else {
            parameter.setDir(dir);
        }

        String pageNo = request.getParameter("pageNo");
        if (StringUtils.isNumeric(pageNo)) {
            parameter.setPageNo(Long.valueOf(pageNo.trim()));
        }

        String pageSize = request.getParameter("pageSize");
        if (StringUtils.isNumeric(pageSize)) {
            parameter.setPageSize(Long.valueOf(pageSize.trim()));
        }

        return parameter;
    }

    public <T> T getParameter(HttpServletRequest request, Class<T> clazz) {
        if (StringUtils.isBlank(request.getContentType()) || !MediaType.APPLICATION_JSON
            .includes(MediaType.parseMediaType(request.getContentType()))) {
            return JSON.parseObject("{}", clazz);
        }

        try {
            T parameter = JSON.parseObject(
                IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8), clazz);

            if (parameter instanceof BaseBO baseBO) {
                if (baseBO.getDateRange() != null && baseBO.getDateRange().length == 2) {
                    baseBO.setStart(baseBO.getDateRange()[0]);
                    baseBO.setEnd(baseBO.getDateRange()[1]);
                }

                String dir = baseBO.getDir();
                if ("ascending".equals(dir)) {
                    baseBO.setDir("ASC");
                } else if ("descending".equals(dir)) {
                    baseBO.setDir("DESC");
                } else {
                    baseBO.setDir(dir);
                }
            }

            return parameter;
        } catch (Exception e) {
            log.error("", e);
        }

        return JSON.parseObject("{}", clazz);
    }

    public <T> List<T> getParameters(HttpServletRequest request, Class<T> clazz) {
        if (StringUtils.isBlank(request.getContentType()) || !MediaType.APPLICATION_JSON
            .includes(MediaType.parseMediaType(request.getContentType()))) {
            return JSON.parseArray("[]", clazz);
        }

        try {
            return JSON.parseArray(
                IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8), clazz);
        } catch (IOException e) {
            log.error("", e);
        }

        return JSON.parseArray("[]", clazz);
    }

    public String getRemoteAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        return StringUtils.isNotEmpty(ip) ? ip : request.getRemoteAddr();
    }

}
