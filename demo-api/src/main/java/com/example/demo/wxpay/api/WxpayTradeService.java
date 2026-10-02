package com.example.demo.wxpay.api;

import com.example.demo.wxpay.api.bo.WxpayTrade;

public interface WxpayTradeService {

    WxpayTrade getTrade(String mchid, String outTradeNo);

    WxpayTrade getTrade(String spMchid, String subMchid, String outTradeNo);

    WxpayTrade getTradeV1(String serialNumber, String nonce, String timestamp, String signature,
                          String body);

    WxpayTrade getTradeV2(String serialNumber, String nonce, String timestamp, String signature,
                          String body);

    WxpayTrade insertTrade(WxpayTrade wxpayNotify);

}
