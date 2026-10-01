package com.example.demo.alipay.service;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.alipay.api.AlipayTradeService;
import com.example.demo.alipay.api.FactoryPaymentCommonService;
import com.example.demo.alipay.api.bo.AlipayTrade;
import com.example.demo.alipay.dao.dataobject.AlipayTradeDO;
import com.example.demo.alipay.dao.mapper.AlipayTradeMapper;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.framework.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AlipayTradeServiceImpl extends ServiceImpl<AlipayTradeMapper, AlipayTradeDO>
                                    implements AlipayTradeService {

    @Autowired
    private FactoryPaymentCommonService factoryPaymentCommonService;

    @Override
    public AlipayTrade getTrade(Map<String, String> parameters) {
        AlipayTrade alipayTrade = new AlipayTrade();
        alipayTrade.setNotifyTime(getDate(parameters.get("notify_time")));
        alipayTrade.setNotifyType(parameters.get("notify_type"));
        alipayTrade.setNotifyId(parameters.get("notify_id"));
        alipayTrade.setAppId(parameters.get("app_id"));
        alipayTrade.setCharset(parameters.get("charset"));
        alipayTrade.setVersion(parameters.get("version"));
        alipayTrade.setSignType(parameters.get("sign_type"));
        alipayTrade.setSign(parameters.get("sign"));
        alipayTrade.setTradeNo(parameters.get("trade_no"));
        alipayTrade.setOutTradeNo(parameters.get("out_trade_no"));
        alipayTrade.setOutBizNo(parameters.get("out_biz_no"));
        alipayTrade.setBuyerId(parameters.get("buyer_id"));
        alipayTrade.setBuyerLogonId(parameters.get("buyer_logon_id"));
        alipayTrade.setSellerId(parameters.get("seller_id"));
        alipayTrade.setSellerEmail(parameters.get("seller_email"));
        alipayTrade.setTradeStatus(parameters.get("trade_status"));
        alipayTrade.setTotalAmount(getBigDecimal(parameters.get("total_amount")));
        alipayTrade.setReceiptAmount(getBigDecimal(parameters.get("receipt_amount")));
        alipayTrade.setInvoiceAmount(getBigDecimal(parameters.get("invoice_amount")));
        alipayTrade.setBuyerPayAmount(getBigDecimal(parameters.get("buyer_pay_amount")));
        alipayTrade.setPointAmount(getBigDecimal(parameters.get("point_amount")));
        alipayTrade.setRefundFee(getBigDecimal(parameters.get("refund_fee")));
        alipayTrade.setSubject(parameters.get("subject"));
        alipayTrade.setBody(parameters.get("body"));
        alipayTrade.setGmtCreate(getDate(parameters.get("gmt_create")));
        alipayTrade.setGmtPayment(getDate(parameters.get("gmt_payment")));
        alipayTrade.setGmtRefund(getDate(parameters.get("gmt_refund")));
        alipayTrade.setGmtClose(getDate(parameters.get("gmt_close")));
        alipayTrade.setFundBillList(parameters.get("fund_bill_list"));
        alipayTrade.setPassbackParams(parameters.get("passback_params"));
        alipayTrade.setVoucherDetailList(parameters.get("voucher_detail_list"));

        try {
            if (!Factory.Payment.Common().verifyNotify(parameters)) {
                throw new RuntimeException("验签失败");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return alipayTrade;
    }

    @Override
    public AlipayTrade getTrade(String appAuthToken, String outTradeNo) {
        AlipayTradeQueryResponse response = factoryPaymentCommonService.query(appAuthToken,
            outTradeNo);

        AlipayTrade alipayTrade = new AlipayTrade();
        alipayTrade.setNotifyId(UUID.randomUUID().toString());
        // alipayTrade.setSign(data.getString("sign"));
        alipayTrade.setTradeNo(response.getTradeNo());
        alipayTrade.setOutTradeNo(response.getOutTradeNo());
        alipayTrade.setBuyerId(response.getBuyerUserId());
        alipayTrade.setBuyerLogonId(response.getBuyerLogonId());
        alipayTrade.setTradeStatus(response.getTradeStatus());
        alipayTrade.setTotalAmount(getBigDecimal(response.getTotalAmount()));
        alipayTrade.setGmtPayment(getDate(response.getSendPayDate())); // TODO

        return alipayTrade;
    }

    @Override
    public AlipayTrade insertTrade(AlipayTrade trade) {
        if (trade == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "参数信息不能为空");
        }

        AlipayTradeDO alipayTradeDO = BeanUtil.copy(trade, AlipayTradeDO.class);

        this.save(alipayTradeDO);

        return trade;
    }

    private Date getDate(String date) {
        if (StringUtils.isBlank(date)) {
            return null;
        }

        return DateUtil.getDateTime(date);
    }

    private BigDecimal getBigDecimal(String val) {
        if (StringUtils.isBlank(val)) {
            return null;
        }

        return new BigDecimal(val);
    }

}
