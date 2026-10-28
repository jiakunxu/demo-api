package com.example.demo.weixin.service;

import com.example.demo.cache.api.RedisService;
import com.example.demo.weixin.api.AccessTokenService;
import com.example.demo.weixin.api.TokenService;
import com.example.demo.weixin.api.bo.token.AccessToken;
import com.example.demo.framework.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author JiakunXu
 */
@Slf4j
@Service("com.example.demo.weixin.service.tokenService")
public class TokenServiceImpl implements TokenService {

    @Autowired
    private AccessTokenService           accessTokenService;

    @Autowired
    private RedisService<String, String> redisService;

    @Override
    public String getToken(String grantType, String appId,
                           String appSecret) throws RuntimeException {
        String key = RedisService.CACHE_KEY_WX_TOKEN + grantType + "&" + appId + "&" + appSecret;

        String token = null;

        try {
            token = redisService.get(key);
        } catch (ServiceException e) {
            log.error(key, e);
        }

        if (StringUtils.isNotBlank(token)) {
            return token;
        }

        AccessToken accessToken = accessTokenService.getAccessToken(grantType, appId, appSecret);

        token = accessToken.getAccessToken();

        try {
            redisService.set(key, token, accessToken.getExpiresIn() - 5 * 60);
        } catch (ServiceException e) {
            log.error(key, e);
        }

        return token;
    }

}
