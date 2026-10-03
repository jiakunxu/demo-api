package com.example.demo.wxpay.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.framework.annotation.NotNull;
import com.example.demo.wxpay.api.RefundService;
import com.example.demo.wxpay.api.WxpayRefundService;
import com.example.demo.wxpay.api.bo.WxpayRefund;
import com.example.demo.wxpay.dao.dataobject.WxpayRefundDO;
import com.example.demo.wxpay.dao.mapper.WxpayRefundMapper;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WxpayRefundServiceImpl extends ServiceImpl<WxpayRefundMapper, WxpayRefundDO>
                                    implements WxpayRefundService {

    @Autowired(required = false)
    private RSAAutoCertificateConfig merchantConfig;

    @Autowired(required = false)
    private RSAAutoCertificateConfig partnerConfig;

    @Autowired
    private RefundService            refundService;

    @Value("${wxpay.merchant.serialNumber}")
    private String                   serialNumber;

    @Value("${wxpay.partner.merchant.serialNumber}")
    private String                   partnerSerialNumber;

    @Override
    public WxpayRefund getRefund(String outRefundNo) {
        if (StringUtils.isBlank(outRefundNo)) {
            return null;
        }

        Refund refund = refundService.queryByOutRefundNo(outRefundNo);

        WxpayRefund wxpayRefund = new WxpayRefund();
        wxpayRefund.setRefundId(refund.getRefundId());
        wxpayRefund.setOutRefundNo(refund.getOutRefundNo());
        wxpayRefund.setTransactionId(refund.getTransactionId());
        wxpayRefund.setOutTradeNo(refund.getOutTradeNo());
        wxpayRefund.setChannel(refund.getChannel() == null ? null : refund.getChannel().name());
        wxpayRefund.setUserReceivedAccount(refund.getUserReceivedAccount());
        wxpayRefund.setSuccessTime(refund.getSuccessTime());
        wxpayRefund.setCreateTime(refund.getCreateTime());
        wxpayRefund.setStatus(refund.getStatus().name());
        wxpayRefund.setFundsAccount(
            refund.getFundsAccount() == null ? null : refund.getFundsAccount().name());
        wxpayRefund.setAmount(JSON.toJSONString(refund.getAmount()));
        wxpayRefund.setPromotionDetail(JSON.toJSONString(refund.getPromotionDetail()));

        return wxpayRefund;
    }

    @Override
    public WxpayRefund getRefund(String subMchid, String outRefundNo) {
        if (StringUtils.isAnyBlank(subMchid, outRefundNo)) {
            return null;
        }

        Refund refund = refundService.queryByOutRefundNo(subMchid, outRefundNo);

        WxpayRefund wxpayRefund = new WxpayRefund();
        wxpayRefund.setRefundId(refund.getRefundId());
        wxpayRefund.setOutRefundNo(refund.getOutRefundNo());
        wxpayRefund.setTransactionId(refund.getTransactionId());
        wxpayRefund.setOutTradeNo(refund.getOutTradeNo());
        wxpayRefund.setChannel(refund.getChannel() == null ? null : refund.getChannel().name());
        wxpayRefund.setUserReceivedAccount(refund.getUserReceivedAccount());
        wxpayRefund.setSuccessTime(refund.getSuccessTime());
        wxpayRefund.setCreateTime(refund.getCreateTime());
        wxpayRefund.setStatus(refund.getStatus().name());
        wxpayRefund.setFundsAccount(
            refund.getFundsAccount() == null ? null : refund.getFundsAccount().name());
        wxpayRefund.setAmount(JSON.toJSONString(refund.getAmount()));
        wxpayRefund.setPromotionDetail(JSON.toJSONString(refund.getPromotionDetail()));

        return wxpayRefund;
    }

    @Override
    public WxpayRefund getRefundV1(String serialNumber, String nonce, String timestamp,
                                   String signature, String body) {
        RequestParam requestParam = new RequestParam.Builder().serialNumber(serialNumber)
            .nonce(nonce).signature(signature).timestamp(timestamp).body(body).build();

        RefundNotification refund = new NotificationParser(merchantConfig).parse(requestParam,
            RefundNotification.class);

        WxpayRefund wxpayRefund = new WxpayRefund();
        wxpayRefund.setRefundId(refund.getRefundId());
        wxpayRefund.setOutRefundNo(refund.getOutRefundNo());
        wxpayRefund.setTransactionId(refund.getTransactionId());
        wxpayRefund.setOutTradeNo(refund.getOutTradeNo());
        wxpayRefund.setChannel(refund.getChannel() == null ? null : refund.getChannel().name());
        wxpayRefund.setUserReceivedAccount(refund.getUserReceivedAccount());
        wxpayRefund.setSuccessTime(refund.getSuccessTime());
        wxpayRefund.setCreateTime(refund.getCreateTime());
        wxpayRefund.setStatus(refund.getRefundStatus().name());
        wxpayRefund.setFundsAccount(
            refund.getFundsAccount() == null ? null : refund.getFundsAccount().name());
        wxpayRefund.setAmount(JSON.toJSONString(refund.getAmount()));
        wxpayRefund.setPromotionDetail(JSON.toJSONString(refund.getPromotionDetail()));

        return wxpayRefund;
    }

    @Override
    public WxpayRefund getRefundV2(String serialNumber, String nonce, String timestamp,
                                   String signature, String body) {
        RequestParam requestParam = new RequestParam.Builder().serialNumber(serialNumber)
            .nonce(nonce).signature(signature).timestamp(timestamp).body(body).build();

        RefundNotification refund = new NotificationParser(partnerConfig).parse(requestParam,
            RefundNotification.class);

        WxpayRefund wxpayRefund = new WxpayRefund();
        wxpayRefund.setRefundId(refund.getRefundId());
        wxpayRefund.setOutRefundNo(refund.getOutRefundNo());
        wxpayRefund.setTransactionId(refund.getTransactionId());
        wxpayRefund.setOutTradeNo(refund.getOutTradeNo());
        wxpayRefund.setChannel(refund.getChannel() == null ? null : refund.getChannel().name());
        wxpayRefund.setUserReceivedAccount(refund.getUserReceivedAccount());
        wxpayRefund.setSuccessTime(refund.getSuccessTime());
        wxpayRefund.setCreateTime(refund.getCreateTime());
        wxpayRefund.setStatus(refund.getRefundStatus().name());
        wxpayRefund.setFundsAccount(
            refund.getFundsAccount() == null ? null : refund.getFundsAccount().name());
        wxpayRefund.setAmount(JSON.toJSONString(refund.getAmount()));
        wxpayRefund.setPromotionDetail(JSON.toJSONString(refund.getPromotionDetail()));

        return wxpayRefund;
    }

    @Override
    public WxpayRefund insertRefund(@NotNull WxpayRefund refund) {
        WxpayRefundDO refundDO = new WxpayRefundDO();
        refundDO.setOutTradeNo(refund.getOutTradeNo());
        refundDO.setOutRefundNo(refund.getOutRefundNo());
        refundDO.setRefund(JSON.toJSONString(refund));
        refundDO.setCreator("系统");

        this.save(refundDO);

        return refund;
    }

}
