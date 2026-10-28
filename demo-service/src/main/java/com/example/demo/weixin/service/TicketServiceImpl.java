package com.example.demo.weixin.service;

import com.example.demo.cache.api.RedisService;
import com.example.demo.weixin.api.JsapiTicketService;
import com.example.demo.weixin.api.TicketService;
import com.example.demo.weixin.api.TokenService;
import com.example.demo.weixin.api.bo.js.JsapiTicket;
import com.example.demo.framework.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author JiakunXu
 */
@Slf4j
@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private JsapiTicketService           jsapiTicketService;

    @Autowired
    private RedisService<String, String> redisService;

    @Autowired
    private TokenService                 tokenService;

    @Override
    public String getTicket(String appId, String appSecret) throws RuntimeException {
        String key = RedisService.CACHE_KEY_WX_TICKET + appId + "&" + appSecret;

        String ticket = null;

        try {
            ticket = redisService.get(key);
        } catch (ServiceException e) {
            log.error(key, e);
        }

        if (StringUtils.isNotBlank(ticket)) {
            return ticket;
        }

        JsapiTicket jsapiTicket = jsapiTicketService
            .getJsapiTicket(tokenService.getToken("client_credential", appId, appSecret));

        ticket = jsapiTicket.getTicket();

        try {
            redisService.set(key, ticket, jsapiTicket.getExpiresIn() - 5 * 60);
        } catch (ServiceException e) {
            log.error(key, e);
        }

        return ticket;
    }

}
