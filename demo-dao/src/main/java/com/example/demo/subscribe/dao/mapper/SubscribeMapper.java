package com.example.demo.subscribe.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.subscribe.dao.dataobject.SubscribeDO;
import com.example.demo.tunnel.dao.dataobject.TunnelDO;

import java.util.List;

/**
 * @author JiakunXu
 */
public interface SubscribeMapper extends BaseMapper<SubscribeDO> {

    /**
     *
     * @param subscribeDO
     * @return
     */
    long countSubscribe0(SubscribeDO subscribeDO);

    /**
     *
     * @param subscribeDO
     * @return
     */
    long countSubscribe1(SubscribeDO subscribeDO);

    /**
     *
     * @param subscribeDO
     * @return
     */
    List<TunnelDO> listSubscribes(SubscribeDO subscribeDO);

    SubscribeDO getSubscribe(SubscribeDO subscribeDO);

}
