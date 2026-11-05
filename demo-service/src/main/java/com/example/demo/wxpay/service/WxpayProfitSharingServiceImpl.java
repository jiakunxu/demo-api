package com.example.demo.wxpay.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.wxpay.api.ProfitsharingService;
import com.example.demo.wxpay.api.WxpayProfitSharingService;
import com.example.demo.wxpay.api.bo.WxpayProfitSharing;
import com.example.demo.wxpay.dao.dataobject.WxpayProfitSharingDO;
import com.example.demo.wxpay.dao.mapper.WxpayProfitSharingMapper;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.profitsharing.model.OrdersEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class WxpayProfitSharingServiceImpl extends
                                           ServiceImpl<WxpayProfitSharingMapper, WxpayProfitSharingDO>
                                           implements WxpayProfitSharingService {

    @Autowired(required = false)
    private RSAAutoCertificateConfig partnerConfig;

    @Autowired
    private ProfitsharingService     profitsharingService;

    @Override
    public WxpayProfitSharing getProfitSharing(String transactionId, String outOrderNo) {
        if (StringUtils.isAnyBlank(transactionId, outOrderNo)) {
            return null;
        }

        OrdersEntity orders = profitsharingService.queryOrder(transactionId, outOrderNo);

        WxpayProfitSharing profitSharing = new WxpayProfitSharing();
        profitSharing.setOrderId(orders.getOrderId());
        profitSharing.setTransactionId(orders.getTransactionId());
        profitSharing.setOutOrderNo(orders.getOutOrderNo());
        profitSharing.setState(orders.getState().name());
        profitSharing.setReceivers(orders.getReceivers() == null ? null
            : JSON.toJSONString(orders.getReceivers()));

        return profitSharing;
    }

    @Override
    public WxpayProfitSharing getProfitSharing(String subMchid, String transactionId,
                                               String outOrderNo) {
        if (StringUtils.isAnyBlank(subMchid, transactionId, outOrderNo)) {
            return null;
        }

        OrdersEntity orders = profitsharingService.queryOrder(subMchid, transactionId, outOrderNo);

        WxpayProfitSharing profitSharing = new WxpayProfitSharing();
        profitSharing.setOrderId(orders.getOrderId());
        profitSharing.setSubMchid(orders.getSubMchid());
        profitSharing.setTransactionId(orders.getTransactionId());
        profitSharing.setOutOrderNo(orders.getOutOrderNo());
        profitSharing.setState(orders.getState().name());
        profitSharing.setReceivers(orders.getReceivers() == null ? null
            : JSON.toJSONString(orders.getReceivers()));

        return profitSharing;
    }

    @Override
    public WxpayProfitSharing getProfitSharing(String serialNumber, String nonce, String timestamp,
                                               String signature, String body) {
        RequestParam requestParam = new RequestParam.Builder().serialNumber(serialNumber)
            .nonce(nonce).signature(signature).timestamp(timestamp).body(body).build();

        Map<?, ?> map = new NotificationParser(partnerConfig).parse(requestParam, Map.class);

        WxpayProfitSharing profitSharing = new WxpayProfitSharing();
        profitSharing.setOrderId(map.get("order_id").toString());
        profitSharing.setMchid(map.containsKey("mchid") ? map.get("mchid").toString() : null);
        profitSharing
            .setSpMchid(map.containsKey("sp_mchid") ? map.get("sp_mchid").toString() : null);
        profitSharing
            .setSubMchid(map.containsKey("sub_mchid") ? map.get("sub_mchid").toString() : null);
        profitSharing.setTransactionId(map.get("transaction_id").toString());
        profitSharing.setOutOrderNo(map.get("out_order_no").toString());
        profitSharing.setState(WxpayProfitSharing.State.FINISHED.value);
        profitSharing.setReceivers(map.get("receiver").toString());
        profitSharing.setSuccessTime(map.get("success_time").toString());

        return profitSharing;
    }

    @Override
    public WxpayProfitSharing insertProfitSharing(WxpayProfitSharing profitSharing) {
        if (profitSharing == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "参数信息不能为空");
        }

        WxpayProfitSharingDO profitSharingDO = BeanUtil.copy(profitSharing,
            WxpayProfitSharingDO.class);

        this.save(profitSharingDO);

        return profitSharing;
    }

    @Override
    public WxpayProfitSharing updateProfitSharing(String orderId,
                                                  WxpayProfitSharing profitSharing) {
        if (StringUtils.isBlank(orderId) || profitSharing == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "参数信息不能为空");
        }

        profitSharing.setOrderId(orderId);

        var updateWrapper = Wrappers.<WxpayProfitSharingDO> lambdaUpdate()
            .eq(WxpayProfitSharingDO::getOrderId, orderId)
            .set(WxpayProfitSharingDO::getOrderId, orderId)
            .set(StringUtils.isNotEmpty(profitSharing.getMchid()), WxpayProfitSharingDO::getMchid,
                profitSharing.getMchid())
            .set(StringUtils.isNotEmpty(profitSharing.getSpMchid()),
                WxpayProfitSharingDO::getSpMchid, profitSharing.getSpMchid())
            .set(StringUtils.isNotEmpty(profitSharing.getSubMchid()),
                WxpayProfitSharingDO::getSubMchid, profitSharing.getSubMchid())
            .set(StringUtils.isNotEmpty(profitSharing.getTransactionId()),
                WxpayProfitSharingDO::getTransactionId, profitSharing.getTransactionId())
            .set(StringUtils.isNotEmpty(profitSharing.getOutOrderNo()),
                WxpayProfitSharingDO::getOutOrderNo, profitSharing.getOutOrderNo())
            .set(StringUtils.isNotEmpty(profitSharing.getState()), WxpayProfitSharingDO::getState,
                profitSharing.getState())
            .set(StringUtils.isNotEmpty(profitSharing.getReceivers()),
                WxpayProfitSharingDO::getReceivers, profitSharing.getReceivers())
            .set(StringUtils.isNotEmpty(profitSharing.getSuccessTime()),
                WxpayProfitSharingDO::getSuccessTime, profitSharing.getSuccessTime());

        if (!this.update(updateWrapper)) {
            this.save(BeanUtil.copy(profitSharing, WxpayProfitSharingDO.class));
        }

        return profitSharing;
    }

}
