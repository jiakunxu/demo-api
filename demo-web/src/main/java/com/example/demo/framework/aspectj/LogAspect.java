package com.example.demo.framework.aspectj;

import com.alibaba.fastjson2.JSON;
import com.example.demo.framework.annotation.Log;
import com.example.demo.operate.api.bo.OperateLog;
import com.example.demo.mq.api.ProducerService;
import com.example.demo.security.api.bo.LoginUser;
import com.example.demo.user.api.bo.User;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private ProducerService producerService;

    @Value("${aliyun.ons.topic}")
    private String          topic;

    @AfterReturning(pointcut = "@annotation(log)", returning = "object")
    public void doAfterReturning(JoinPoint joinPoint, Log log, Object object) {
        handle(joinPoint.getTarget().getClass().getSimpleName(),
            OperateLog.builder().module(log.module()).operateType(log.desc()).build(),
            OperateLog.Status.SUCCESS.value, null);
    }

    @AfterThrowing(value = "@annotation(log)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log log, Exception e) {
        handle(joinPoint.getTarget().getClass().getSimpleName(),
            OperateLog.builder().module(log.module()).operateType(log.desc()).build(),
            OperateLog.Status.FAIL.value, e.getMessage());
    }

    private void handle(String clazz, OperateLog log, String status, String errMsg) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = "anonymousUser".equals(authentication.getName())
            ? User.builder().name("anonymous").build()
            : (LoginUser) authentication.getPrincipal();

        log.setOperator(user.getId() == null ? user.getName() : user.getId().toString());
        log.setContent(null);
        log.setOperateTime(new Date());
        log.setClazz(clazz);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
            .getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();

        log.setRequestUri(request == null ? null : request.getRequestURI());
        log.setRequestMethod(request == null ? null : request.getMethod());

        if (request != null) {
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                try {
                    log.setRequestParams(
                        new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8));
                } catch (Exception ignored) {
                }
            }
            if (StringUtils.isBlank(log.getRequestParams())) {
                var params = request.getParameterMap();
                log.setRequestParams(
                    params.isEmpty() ? request.getQueryString() : JSON.toJSONString(params));
            }
        }

        log.setIp(request == null ? null : getRemoteAddr(request));
        log.setIpAddr(null);
        log.setStatus(status);
        log.setErrMsg(errMsg);

        if (OperateLog.Status.FAIL.value.equals(status)) {
            // producerService.sendOneway(topic, "operate.log.save", JSON.toJSONBytes(log), null);
        }
    }

    private String getRemoteAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        return StringUtils.isNotEmpty(ip) ? ip : request.getRemoteAddr();
    }

}
