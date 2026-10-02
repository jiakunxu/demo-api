package com.example.demo.subscribe.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.mq.api.ProducerService;
import com.example.demo.socket.api.bo.Message;
import com.example.demo.subscribe.api.SubscribeService;
import com.example.demo.subscribe.api.bo.Subscribe;
import com.example.demo.subscribe.dao.dataobject.SubscribeDO;
import com.example.demo.subscribe.dao.mapper.SubscribeMapper;
import com.example.demo.tunnel.api.bo.Tunnel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

/**
 * @author JiakunXu
 */
@Slf4j
@Service
public class SubscribeServiceImpl extends ServiceImpl<SubscribeMapper, SubscribeDO>
                                  implements SubscribeService {

    @Autowired
    private ProducerService producerService;

    @Override
    public long countSubscribe(BigInteger userId, String appId, String scene, String sceneId) {
        if (userId == null || StringUtils.isBlank(scene) || StringUtils.isBlank(sceneId)) {
            return 0;
        }

        SubscribeDO subscribeDO = new SubscribeDO();
        subscribeDO.setUserId(userId);
        subscribeDO.setAppId(appId);
        subscribeDO.setScene(scene);
        subscribeDO.setSceneId(new BigInteger(sceneId));

        return this.baseMapper.countSubscribe0(subscribeDO);
    }

    @Override
    public long countSubscribe(BigInteger userId, String scene, String sceneId) {
        if (userId == null || StringUtils.isBlank(scene) || StringUtils.isBlank(sceneId)) {
            return 0;
        }

        SubscribeDO subscribeDO = new SubscribeDO();
        subscribeDO.setUserId(userId);
        subscribeDO.setScene(scene);
        subscribeDO.setSceneId(new BigInteger(sceneId));

        return this.baseMapper.countSubscribe1(subscribeDO);
    }

    @Override
    public List<Tunnel> listSubscribes(String scene, String sceneId, Subscribe subscribe) {
        if (StringUtils.isBlank(scene) || StringUtils.isBlank(sceneId) || subscribe == null) {
            return List.of();
        }

        subscribe.setScene(scene);
        subscribe.setSceneId(new BigInteger(sceneId));

        List<Tunnel> list = BeanUtil.copy(
            this.baseMapper.listSubscribes(BeanUtil.copy(subscribe, SubscribeDO.class)),
            Tunnel.class);

        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }

        return list;
    }

    @Override
    public Subscribe getSubscribe(BigInteger userId, String appId, String scene, String sceneId) {
        if (userId == null || StringUtils.isBlank(appId) || StringUtils.isBlank(scene)
            || StringUtils.isBlank(sceneId)) {
            return null;
        }

        SubscribeDO subscribeDO = new SubscribeDO();
        subscribeDO.setUserId(userId);
        subscribeDO.setAppId(appId);
        subscribeDO.setScene(scene);
        subscribeDO.setSceneId(new BigInteger(sceneId));

        return BeanUtil.copy(this.baseMapper.getSubscribe(subscribeDO), Subscribe.class);
    }

    @Override
    public Subscribe insertSubscribe(BigInteger userId, String appId, String scene,
                                     String sceneId) {
        if (userId == null || StringUtils.isBlank(appId) || StringUtils.isBlank(scene)) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "参数信息不能为空");
        }

        SubscribeDO subscribeDO = new SubscribeDO();
        subscribeDO.setUserId(userId);
        subscribeDO.setAppId(appId);
        subscribeDO.setScene(scene);

        if ("app".equals(scene)) {
            sceneId = userId.toString();
        }

        if (StringUtils.isBlank(sceneId)) {
            sceneId = "0";
        }

        subscribeDO.setSceneId(new BigInteger(sceneId));
        subscribeDO.setCreator(userId.toString());

        Subscribe s = getSubscribe(userId, appId, scene, sceneId);

        if (s != null) {
            return s;
        }

        this.save(subscribeDO);

        return BeanUtil.copy(subscribeDO, Subscribe.class);
    }

    @Override
    public void sendMessage(String scene, String sceneId, Message message) {
        if (StringUtils.isBlank(scene) || StringUtils.isBlank(sceneId) || message == null) {
            return;
        }

        for (int i = 1;; i++) {
            Subscribe subscribe = new Subscribe();
            subscribe.setPageNo((long) i);
            subscribe.setPageSize(20L);

            List<Tunnel> list = listSubscribes(scene, sceneId, subscribe);

            if (CollectionUtils.isEmpty(list)) {
                return;
            }

            for (Tunnel tunnel : list) {
                producerService.send("topic", "web.socket", JSON.toJSONBytes(message),
                    tunnel.getTunnelId());
            }

            if (list.size() < 20) {
                return;
            }
        }
    }

}
