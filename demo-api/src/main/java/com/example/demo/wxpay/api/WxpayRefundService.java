package com.example.demo.wxpay.api;

import com.example.demo.wxpay.api.bo.WxpayRefund;

public interface WxpayRefundService {

    WxpayRefund getRefund(String outRefundNo);

    WxpayRefund getRefund(String subMchid, String outRefundNo);

    WxpayRefund getRefundV1(String serialNumber, String nonce, String timestamp, String signature,
                            String body);

    WxpayRefund getRefundV2(String serialNumber, String nonce, String timestamp, String signature,
                            String body);

    WxpayRefund insertRefund(WxpayRefund wxpayRefund);

}
