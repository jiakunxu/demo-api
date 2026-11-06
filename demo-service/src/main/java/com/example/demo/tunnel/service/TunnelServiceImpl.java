package com.example.demo.tunnel.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.demo.framework.annotation.NotBlank;
import com.example.demo.framework.annotation.NotNull;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.tunnel.api.TunnelService;
import com.example.demo.tunnel.api.bo.Tunnel;
import com.example.demo.tunnel.dao.dataobject.TunnelDO;
import com.example.demo.tunnel.dao.mapper.TunnelMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.UUID;

/**
 * @author JiakunXu
 */
@Slf4j
@Service
public class TunnelServiceImpl extends ServiceImpl<TunnelMapper, TunnelDO>
                               implements TunnelService {

    @Override
    public Tunnel insertTunnel(@NotNull BigInteger userId, String host, @NotBlank String createBy) {
        TunnelDO tunnelDO = new TunnelDO();
        tunnelDO.setUserId(userId);
        tunnelDO.setTunnelId(UUID.randomUUID().toString());
        tunnelDO.setHost(host);
        tunnelDO.setCreateBy(createBy);

        this.save(tunnelDO);

        return BeanUtil.copy(tunnelDO, Tunnel.class);
    }

    @Override
    public Tunnel deleteTunnel(@NotBlank String tunnelId, @NotBlank String updateBy) {
        var updateWrapper = Wrappers.lambdaUpdate(TunnelDO.class)
            .eq(TunnelDO::getTunnelId, tunnelId)
            .set(TunnelDO::getDeleted, true).set(TunnelDO::getUpdateBy, updateBy);

        if (!this.update(updateWrapper)) {
            log.error("{},{}", tunnelId, updateBy);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }

        return Tunnel.builder().tunnelId(tunnelId).build();
    }

}
