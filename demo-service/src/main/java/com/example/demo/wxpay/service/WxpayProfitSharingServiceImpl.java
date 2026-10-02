package com.example.demo.wxpay.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
    public WxpayProfitSharing getWxpayProfitSharing(String transactionId, String outOrderNo) {
        if (StringUtils.isAnyBlank(transactionId, outOrderNo)) {
            return null;
        }

        OrdersEntity orders = profitsharingService.queryOrder(transactionId, outOrderNo);

        WxpayProfitSharing wxpayProfitSharing = new WxpayProfitSharing();
        wxpayProfitSharing.setOrderId(orders.getOrderId());
        wxpayProfitSharing.setTransactionId(orders.getTransactionId());
        wxpayProfitSharing.setOutOrderNo(orders.getOutOrderNo());
        wxpayProfitSharing.setState(orders.getState().name());
        wxpayProfitSharing.setReceivers(JSON.toJSONString(orders.getReceivers()));

        return wxpayProfitSharing;
    }

    @Override
    public WxpayProfitSharing getWxpayProfitSharing(String subMchid, String transactionId,
                                                    String outOrderNo) {
        if (StringUtils.isAnyBlank(subMchid, transactionId, outOrderNo)) {
            return null;
        }

        OrdersEntity orders = profitsharingService.queryOrder(subMchid, transactionId, outOrderNo);

        WxpayProfitSharing wxpayProfitSharing = new WxpayProfitSharing();
        wxpayProfitSharing.setOrderId(orders.getOrderId());
        wxpayProfitSharing.setSubMchid(orders.getSubMchid());
        wxpayProfitSharing.setTransactionId(orders.getTransactionId());
        wxpayProfitSharing.setOutOrderNo(orders.getOutOrderNo());
        wxpayProfitSharing.setState(orders.getState().name());
        wxpayProfitSharing.setReceivers(JSON.toJSONString(orders.getReceivers()));

        return wxpayProfitSharing;
    }

    @Override
    public WxpayProfitSharing getWxpayProfitSharing(String serialNumber, String nonce,
                                                    String timestamp, String signature,
                                                    String body) {
        RequestParam requestParam = new RequestParam.Builder().serialNumber(serialNumber)
            .nonce(nonce).signature(signature).timestamp(timestamp).body(body).build();

        Map<?, ?> map = new NotificationParser(partnerConfig).parse(requestParam, Map.class);

        WxpayProfitSharing wxpayProfitSharing = new WxpayProfitSharing();
        wxpayProfitSharing.setOrderId(map.get("order_id").toString());
        wxpayProfitSharing.setMchid(map.containsKey("mchid") ? map.get("mchid").toString() : null);
        wxpayProfitSharing
            .setSpMchid(map.containsKey("sp_mchid") ? map.get("sp_mchid").toString() : null);
        wxpayProfitSharing
            .setSubMchid(map.containsKey("sub_mchid") ? map.get("sub_mchid").toString() : null);
        wxpayProfitSharing.setTransactionId(map.get("transaction_id").toString());
        wxpayProfitSharing.setOutOrderNo(map.get("out_order_no").toString());
        wxpayProfitSharing.setState(WxpayProfitSharing.State.FINISHED.value);
        wxpayProfitSharing.setReceivers(map.get("receiver").toString());
        wxpayProfitSharing.setSuccessTime(map.get("success_time").toString());

        return wxpayProfitSharing;
    }

    @Override
    public WxpayProfitSharing insertWxpayProfitSharing(WxpayProfitSharing wxpayProfitSharing) {
        if (wxpayProfitSharing == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "参数信息不能为空");
        }

        WxpayProfitSharingDO wxpayProfitSharingDO = BeanUtil.copy(wxpayProfitSharing,
            WxpayProfitSharingDO.class);

        this.save(wxpayProfitSharingDO);

        return wxpayProfitSharing;
    }

    @Override
    public WxpayProfitSharing updateWxpayProfitSharing(String orderId,
                                                       WxpayProfitSharing wxpayProfitSharing) {
        if (StringUtils.isBlank(orderId) || wxpayProfitSharing == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "参数信息不能为空");
        }

        wxpayProfitSharing.setOrderId(orderId);

        var updateWrapper = Wrappers.<WxpayProfitSharingDO> lambdaUpdate()
            .eq(WxpayProfitSharingDO::getOrderId, orderId)
            .set(WxpayProfitSharingDO::getOrderId, orderId)
            .set(StringUtils.isNotEmpty(wxpayProfitSharing.getMchid()),
                WxpayProfitSharingDO::getMchid, wxpayProfitSharing.getMchid())
            .set(StringUtils.isNotEmpty(wxpayProfitSharing.getSpMchid()),
                WxpayProfitSharingDO::getSpMchid, wxpayProfitSharing.getSpMchid())
            .set(StringUtils.isNotEmpty(wxpayProfitSharing.getSubMchid()),
                WxpayProfitSharingDO::getSubMchid, wxpayProfitSharing.getSubMchid())
            .set(StringUtils.isNotEmpty(wxpayProfitSharing.getTransactionId()),
                WxpayProfitSharingDO::getTransactionId, wxpayProfitSharing.getTransactionId())
            .set(StringUtils.isNotEmpty(wxpayProfitSharing.getOutOrderNo()),
                WxpayProfitSharingDO::getOutOrderNo, wxpayProfitSharing.getOutOrderNo())
            .set(StringUtils.isNotEmpty(wxpayProfitSharing.getState()),
                WxpayProfitSharingDO::getState, wxpayProfitSharing.getState())
            .set(StringUtils.isNotEmpty(wxpayProfitSharing.getReceivers()),
                WxpayProfitSharingDO::getReceivers, wxpayProfitSharing.getReceivers())
            .set(StringUtils.isNotEmpty(wxpayProfitSharing.getSuccessTime()),
                WxpayProfitSharingDO::getSuccessTime, wxpayProfitSharing.getSuccessTime());

        if (!this.update(updateWrapper)) {
            this.save(BeanUtil.copy(wxpayProfitSharing, WxpayProfitSharingDO.class));
        }

        return wxpayProfitSharing;
    }

}
