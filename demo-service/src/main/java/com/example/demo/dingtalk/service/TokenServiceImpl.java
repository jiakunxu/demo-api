package com.example.demo.dingtalk.service;

import com.example.demo.cache.api.RedisService;
import com.example.demo.dingtalk.api.AccessTokenService;
import com.example.demo.dingtalk.api.TokenService;
import com.example.demo.dingtalk.api.bo.AccessToken;
import com.example.demo.framework.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service("com.example.demo.dingtalk.service.tokenService")
public class TokenServiceImpl implements TokenService {

    @Autowired
    private AccessTokenService           accessTokenService;

    @Autowired
    private RedisService<String, String> redisService;

    @Override
    public String getToken(String appKey, String appSecret) throws RuntimeException {
        String key = RedisService.CACHE_KEY_DD_TOKEN + appKey + "&" + appSecret;

        String token = null;

        try {
            token = redisService.get(key);
        } catch (ServiceException e) {
            log.error("{}", key, e);
        }

        if (StringUtils.isNotBlank(token)) {
            return token;
        }

        AccessToken accessToken = accessTokenService.getAccessToken(appKey, appSecret);

        token = accessToken.getAccessToken();

        try {
            redisService.set(key, token, accessToken.getExpireIn() - 5 * 60);
        } catch (ServiceException e) {
            log.error("{}", key, e);
        }

        return token;
    }

}
