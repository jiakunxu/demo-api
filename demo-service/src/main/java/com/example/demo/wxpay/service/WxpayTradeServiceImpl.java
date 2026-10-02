package com.example.demo.wxpay.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.wxpay.api.JsapiService;
import com.example.demo.wxpay.api.PartnerJsapiService;
import com.example.demo.wxpay.api.WxpayTradeService;
import com.example.demo.wxpay.api.bo.WxpayTrade;
import com.example.demo.wxpay.dao.dataobject.WxpayTradeDO;
import com.example.demo.wxpay.dao.mapper.WxpayTradeMapper;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.Notification;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class WxpayTradeServiceImpl extends ServiceImpl<WxpayTradeMapper, WxpayTradeDO>
                                   implements WxpayTradeService {

    @Autowired(required = false)
    private RSAAutoCertificateConfig merchantConfig;

    @Autowired(required = false)
    private RSAAutoCertificateConfig partnerConfig;

    @Autowired
    private JsapiService             jsapiService;

    @Autowired
    private PartnerJsapiService      partnerJsapiService;

    @Value("${wxpay.merchant.serialNumber}")
    private String                   serialNumber;

    @Value("${wxpay.partner.merchant.serialNumber}")
    private String                   partnerSerialNumber;

    @Override
    public WxpayTrade getTrade(String mchid, String outTradeNo) {
        com.wechat.pay.java.service.payments.model.Transaction transaction = jsapiService
            .queryOrderByOutTradeNo(mchid, outTradeNo);

        WxpayTrade wxpayTrade = new WxpayTrade();
        wxpayTrade.setId(String.valueOf(UUID.randomUUID()));
        wxpayTrade.setAppid(transaction.getAppid());
        wxpayTrade.setMchid(transaction.getMchid());
        wxpayTrade.setOutTradeNo(transaction.getOutTradeNo());
        wxpayTrade.setTransactionId(transaction.getTransactionId());
        wxpayTrade.setTradeType(
            transaction.getTradeType() == null ? null : transaction.getTradeType().name());
        wxpayTrade.setTradeState(transaction.getTradeState().name());
        wxpayTrade.setTradeStateDesc(transaction.getTradeStateDesc());
        wxpayTrade.setBankType(transaction.getBankType());
        wxpayTrade.setAttach(transaction.getAttach());
        wxpayTrade.setSuccessTime(transaction.getSuccessTime());
        wxpayTrade.setPayer(JSON.toJSONString(transaction.getPayer()));
        wxpayTrade.setAmount(JSON.toJSONString(transaction.getAmount()));
        wxpayTrade.setSceneInfo(null);
        wxpayTrade.setPromotionDetail(JSON.toJSONString(transaction.getPromotionDetail()));

        return wxpayTrade;
    }

    @Override
    public WxpayTrade getTrade(String spMchid, String subMchid, String outTradeNo) {
        com.wechat.pay.java.service.partnerpayments.jsapi.model.Transaction transaction = partnerJsapiService
            .queryOrderByOutTradeNo(spMchid, subMchid, outTradeNo);

        WxpayTrade wxpayTrade = new WxpayTrade();
        wxpayTrade.setId(String.valueOf(UUID.randomUUID()));
        wxpayTrade.setSpAppid(transaction.getSpAppid());
        wxpayTrade.setSpMchid(transaction.getSpMchid());
        wxpayTrade.setSubAppid(transaction.getSubAppid());
        wxpayTrade.setSubMchid(transaction.getSubMchid());
        wxpayTrade.setOutTradeNo(transaction.getOutTradeNo());
        wxpayTrade.setTransactionId(transaction.getTransactionId());
        wxpayTrade.setTradeType(
            transaction.getTradeType() == null ? null : transaction.getTradeType().name());
        wxpayTrade.setTradeState(transaction.getTradeState().name());
        wxpayTrade.setTradeStateDesc(transaction.getTradeStateDesc());
        wxpayTrade.setBankType(transaction.getBankType());
        wxpayTrade.setAttach(transaction.getAttach());
        wxpayTrade.setSuccessTime(transaction.getSuccessTime());
        wxpayTrade.setPayer(JSON.toJSONString(transaction.getPayer()));
        wxpayTrade.setAmount(JSON.toJSONString(transaction.getAmount()));
        wxpayTrade.setSceneInfo(null);
        wxpayTrade.setPromotionDetail(JSON.toJSONString(transaction.getPromotionDetail()));

        return wxpayTrade;
    }

    @Override
    public WxpayTrade getTradeV1(String serialNumber, String nonce, String timestamp,
                                 String signature, String body) {
        RequestParam requestParam = new RequestParam.Builder().serialNumber(serialNumber)
            .nonce(nonce).signature(signature).timestamp(timestamp).body(body).build();

        Notification notification = JSON.parseObject(requestParam.getBody(), Notification.class);
        com.wechat.pay.java.service.payments.model.Transaction transaction = new NotificationParser(
            merchantConfig)
            .parse(requestParam, com.wechat.pay.java.service.payments.model.Transaction.class);

        WxpayTrade wxpayTrade = new WxpayTrade();
        wxpayTrade.setId(notification.getId());
        wxpayTrade.setCreateTime(notification.getCreateTime());
        wxpayTrade.setEventType(notification.getEventType());
        wxpayTrade.setSummary(notification.getSummary());
        wxpayTrade.setResourceType(notification.getResourceType());
        wxpayTrade.setAppid(transaction.getAppid());
        wxpayTrade.setMchid(transaction.getMchid());
        wxpayTrade.setOutTradeNo(transaction.getOutTradeNo());
        wxpayTrade.setTransactionId(transaction.getTransactionId());
        wxpayTrade.setTradeType(
            transaction.getTradeType() == null ? null : transaction.getTradeType().name());
        wxpayTrade.setTradeState(transaction.getTradeState().name());
        wxpayTrade.setTradeStateDesc(transaction.getTradeStateDesc());
        wxpayTrade.setBankType(transaction.getBankType());
        wxpayTrade.setAttach(transaction.getAttach());
        wxpayTrade.setSuccessTime(transaction.getSuccessTime());
        wxpayTrade.setPayer(JSON.toJSONString(transaction.getPayer()));
        wxpayTrade.setAmount(JSON.toJSONString(transaction.getAmount()));
        wxpayTrade.setSceneInfo(null);
        wxpayTrade.setPromotionDetail(JSON.toJSONString(transaction.getPromotionDetail()));

        return wxpayTrade;
    }

    @Override
    public WxpayTrade getTradeV2(String serialNumber, String nonce, String timestamp,
                                 String signature, String body) {
        RequestParam requestParam = new RequestParam.Builder().serialNumber(serialNumber)
            .nonce(nonce).signature(signature).timestamp(timestamp).body(body).build();

        Notification notification = JSON.parseObject(requestParam.getBody(), Notification.class);
        com.wechat.pay.java.service.partnerpayments.jsapi.model.Transaction transaction = new NotificationParser(
            partnerConfig).parse(requestParam,
                com.wechat.pay.java.service.partnerpayments.jsapi.model.Transaction.class);

        WxpayTrade wxpayTrade = new WxpayTrade();
        wxpayTrade.setId(notification.getId());
        wxpayTrade.setCreateTime(notification.getCreateTime());
        wxpayTrade.setEventType(notification.getEventType());
        wxpayTrade.setSummary(notification.getSummary());
        wxpayTrade.setResourceType(notification.getResourceType());
        wxpayTrade.setSpAppid(transaction.getSpAppid());
        wxpayTrade.setSpMchid(transaction.getSpMchid());
        wxpayTrade.setSubAppid(transaction.getSubAppid());
        wxpayTrade.setSubMchid(transaction.getSubMchid());
        wxpayTrade.setOutTradeNo(transaction.getOutTradeNo());
        wxpayTrade.setTransactionId(transaction.getTransactionId());
        wxpayTrade.setTradeType(
            transaction.getTradeType() == null ? null : transaction.getTradeType().name());
        wxpayTrade.setTradeState(transaction.getTradeState().name());
        wxpayTrade.setTradeStateDesc(transaction.getTradeStateDesc());
        wxpayTrade.setBankType(transaction.getBankType());
        wxpayTrade.setAttach(transaction.getAttach());
        wxpayTrade.setSuccessTime(transaction.getSuccessTime());
        wxpayTrade.setPayer(JSON.toJSONString(transaction.getPayer()));
        wxpayTrade.setAmount(JSON.toJSONString(transaction.getAmount()));
        wxpayTrade.setSceneInfo(null);
        wxpayTrade.setPromotionDetail(JSON.toJSONString(transaction.getPromotionDetail()));

        return wxpayTrade;
    }

    @Override
    public WxpayTrade insertTrade(@NotNull WxpayTrade trade) {
        WxpayTradeDO tradeDO = new WxpayTradeDO();
        tradeDO.setOutTradeNo(trade.getOutTradeNo());
        tradeDO.setTrade(JSON.toJSONString(trade));
        tradeDO.setCreator("系统");

        this.save(BeanUtil.copy(trade, WxpayTradeDO.class));

        return trade;
    }

}
