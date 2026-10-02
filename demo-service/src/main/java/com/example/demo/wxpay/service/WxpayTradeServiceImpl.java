package com.example.demo.wxpay.service;

import com.alibaba.fastjson2.JSON;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.wxpay.api.JsapiService;
import com.example.demo.wxpay.api.PartnerJsapiService;
import com.example.demo.wxpay.api.WxpayTradeService;
import com.example.demo.wxpay.api.bo.WxpayNotify;
import com.example.demo.wxpay.dao.mapper.WxpayTradeMapper;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.Notification;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class WxpayTradeServiceImpl implements WxpayTradeService {

    @Autowired(required = false)
    private RSAAutoCertificateConfig merchantConfig;

    @Autowired(required = false)
    private RSAAutoCertificateConfig partnerConfig;

    @Autowired
    private JsapiService             jsapiService;

    @Autowired
    private PartnerJsapiService      partnerJsapiService;

    @Autowired
    private WxpayTradeMapper         wxpayTradeMapper;

    @Value("${wxpay.merchant.serialNumber}")
    private String                   serialNumber;

    @Value("${wxpay.partner.merchant.serialNumber}")
    private String                   partnerSerialNumber;

    @Override
    public WxpayNotify getTrade(String mchid, String outTradeNo) {
        com.wechat.pay.java.service.payments.model.Transaction transaction = jsapiService
            .queryOrderByOutTradeNo(mchid, outTradeNo);

        WxpayNotify wxpayNotify = new WxpayNotify();
        wxpayNotify.setId(String.valueOf(UUID.randomUUID()));
        wxpayNotify.setAppid(transaction.getAppid());
        wxpayNotify.setMchid(transaction.getMchid());
        wxpayNotify.setOutTradeNo(transaction.getOutTradeNo());
        wxpayNotify.setTransactionId(transaction.getTransactionId());
        wxpayNotify.setTradeType(
            transaction.getTradeType() == null ? null : transaction.getTradeType().name());
        wxpayNotify.setTradeState(transaction.getTradeState().name());
        wxpayNotify.setTradeStateDesc(transaction.getTradeStateDesc());
        wxpayNotify.setBankType(transaction.getBankType());
        wxpayNotify.setAttach(transaction.getAttach());
        wxpayNotify.setSuccessTime(transaction.getSuccessTime());
        wxpayNotify.setPayer(JSON.toJSONString(transaction.getPayer()));
        wxpayNotify.setAmount(JSON.toJSONString(transaction.getAmount()));
        wxpayNotify.setSceneInfo(null);
        wxpayNotify.setPromotionDetail(JSON.toJSONString(transaction.getPromotionDetail()));

        return wxpayNotify;
    }

    @Override
    public WxpayNotify getTrade(String spMchid, String subMchid, String outTradeNo) {
        com.wechat.pay.java.service.partnerpayments.jsapi.model.Transaction transaction = partnerJsapiService
            .queryOrderByOutTradeNo(spMchid, subMchid, outTradeNo);

        WxpayNotify wxpayNotify = new WxpayNotify();
        wxpayNotify.setId(String.valueOf(UUID.randomUUID()));
        wxpayNotify.setSpAppid(transaction.getSpAppid());
        wxpayNotify.setSpMchid(transaction.getSpMchid());
        wxpayNotify.setSubAppid(transaction.getSubAppid());
        wxpayNotify.setSubMchid(transaction.getSubMchid());
        wxpayNotify.setOutTradeNo(transaction.getOutTradeNo());
        wxpayNotify.setTransactionId(transaction.getTransactionId());
        wxpayNotify.setTradeType(
            transaction.getTradeType() == null ? null : transaction.getTradeType().name());
        wxpayNotify.setTradeState(transaction.getTradeState().name());
        wxpayNotify.setTradeStateDesc(transaction.getTradeStateDesc());
        wxpayNotify.setBankType(transaction.getBankType());
        wxpayNotify.setAttach(transaction.getAttach());
        wxpayNotify.setSuccessTime(transaction.getSuccessTime());
        wxpayNotify.setPayer(JSON.toJSONString(transaction.getPayer()));
        wxpayNotify.setAmount(JSON.toJSONString(transaction.getAmount()));
        wxpayNotify.setSceneInfo(null);
        wxpayNotify.setPromotionDetail(JSON.toJSONString(transaction.getPromotionDetail()));

        return wxpayNotify;
    }

    @Override
    public WxpayNotify getTradeV1(String serialNumber, String nonce, String timestamp,
                                  String signature, String body) {
        RequestParam requestParam = new RequestParam.Builder().serialNumber(serialNumber)
            .nonce(nonce).signature(signature).timestamp(timestamp).body(body).build();

        Notification notification = JSON.parseObject(requestParam.getBody(), Notification.class);
        com.wechat.pay.java.service.payments.model.Transaction transaction = new NotificationParser(
            merchantConfig)
            .parse(requestParam, com.wechat.pay.java.service.payments.model.Transaction.class);

        WxpayNotify wxpayNotify = new WxpayNotify();
        wxpayNotify.setId(notification.getId());
        wxpayNotify.setCreateTime(notification.getCreateTime());
        wxpayNotify.setEventType(notification.getEventType());
        wxpayNotify.setSummary(notification.getSummary());
        wxpayNotify.setResourceType(notification.getResourceType());
        wxpayNotify.setAppid(transaction.getAppid());
        wxpayNotify.setMchid(transaction.getMchid());
        wxpayNotify.setOutTradeNo(transaction.getOutTradeNo());
        wxpayNotify.setTransactionId(transaction.getTransactionId());
        wxpayNotify.setTradeType(
            transaction.getTradeType() == null ? null : transaction.getTradeType().name());
        wxpayNotify.setTradeState(transaction.getTradeState().name());
        wxpayNotify.setTradeStateDesc(transaction.getTradeStateDesc());
        wxpayNotify.setBankType(transaction.getBankType());
        wxpayNotify.setAttach(transaction.getAttach());
        wxpayNotify.setSuccessTime(transaction.getSuccessTime());
        wxpayNotify.setPayer(JSON.toJSONString(transaction.getPayer()));
        wxpayNotify.setAmount(JSON.toJSONString(transaction.getAmount()));
        wxpayNotify.setSceneInfo(null);
        wxpayNotify.setPromotionDetail(JSON.toJSONString(transaction.getPromotionDetail()));

        return wxpayNotify;
    }

    @Override
    public WxpayNotify getTradeV2(String serialNumber, String nonce, String timestamp,
                                  String signature, String body) {
        RequestParam requestParam = new RequestParam.Builder().serialNumber(serialNumber)
            .nonce(nonce).signature(signature).timestamp(timestamp).body(body).build();

        Notification notification = JSON.parseObject(requestParam.getBody(), Notification.class);
        com.wechat.pay.java.service.partnerpayments.jsapi.model.Transaction transaction = new NotificationParser(
            partnerConfig).parse(requestParam,
                com.wechat.pay.java.service.partnerpayments.jsapi.model.Transaction.class);

        WxpayNotify wxpayNotify = new WxpayNotify();
        wxpayNotify.setId(notification.getId());
        wxpayNotify.setCreateTime(notification.getCreateTime());
        wxpayNotify.setEventType(notification.getEventType());
        wxpayNotify.setSummary(notification.getSummary());
        wxpayNotify.setResourceType(notification.getResourceType());
        wxpayNotify.setSpAppid(transaction.getSpAppid());
        wxpayNotify.setSpMchid(transaction.getSpMchid());
        wxpayNotify.setSubAppid(transaction.getSubAppid());
        wxpayNotify.setSubMchid(transaction.getSubMchid());
        wxpayNotify.setOutTradeNo(transaction.getOutTradeNo());
        wxpayNotify.setTransactionId(transaction.getTransactionId());
        wxpayNotify.setTradeType(
            transaction.getTradeType() == null ? null : transaction.getTradeType().name());
        wxpayNotify.setTradeState(transaction.getTradeState().name());
        wxpayNotify.setTradeStateDesc(transaction.getTradeStateDesc());
        wxpayNotify.setBankType(transaction.getBankType());
        wxpayNotify.setAttach(transaction.getAttach());
        wxpayNotify.setSuccessTime(transaction.getSuccessTime());
        wxpayNotify.setPayer(JSON.toJSONString(transaction.getPayer()));
        wxpayNotify.setAmount(JSON.toJSONString(transaction.getAmount()));
        wxpayNotify.setSceneInfo(null);
        wxpayNotify.setPromotionDetail(JSON.toJSONString(transaction.getPromotionDetail()));

        return wxpayNotify;
    }

    @Override
    public WxpayNotify insertTrade(WxpayNotify wxpayNotify) {
        if (wxpayNotify == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "参数信息不能为空");
        }

        WxpayNotifyDO wxpayNotifyDO = BeanUtil.copy(wxpayNotify, WxpayNotifyDO.class);

        try {
            wxpayNotifyMapper.insert(wxpayNotifyDO);
        } catch (Exception e) {
            log.error("{}", wxpayNotifyDO, e);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "信息创建失败，请稍后再试");
        }

        return wxpayNotify;
    }

}
