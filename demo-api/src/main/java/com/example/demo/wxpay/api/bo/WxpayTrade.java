package com.example.demo.wxpay.api.bo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class WxpayTrade implements Serializable {

    @Serial
    private static final long serialVersionUID = -5946034537353065485L;

    /**
     * 通知创建时间
     */
    private String            createTime;

    /**
     * 通知类型
     */
    private String            eventType;

    /**
     * 通知数据类型
     */
    private String            resourceType;

    /**
     * 回调摘要
     */
    private String            summary;

    /**
     * 应用ID
     */
    private String            appid;

    /**
     * 商户号
     */
    private String            mchid;

    /**
     * 商户订单号
     */
    private String            outTradeNo;

    /**
     * 微信支付订单号
     */
    private String            transactionId;

    /**
     * 交易类型
     */
    private String            tradeType;

    /**
     * 交易状态
     */
    private String            tradeState;

    /**
     * 交易状态描述
     */
    private String            tradeStateDesc;

    /**
     * 付款银行
     */
    private String            bankType;

    /**
     * 附加数据
     */
    private String            attach;

    /**
     * 支付完成时间
     */
    private String            successTime;

    /**
     * 支付者
     */
    private String            payer;

    /**
     * 订单金额
     */
    private String            amount;

    /**
     * 场景信息
     */
    private String            sceneInfo;

    /**
     * 优惠功能
     */
    private String            promotionDetail;

    private String            spAppid;

    private String            spMchid;

    private String            subAppid;

    private String            subMchid;

    public enum TradeState {
                            /**
                             * 交易状态
                             */
                            SUCCESS("SUCCESS", "支付成功"),

                            REFUND("REFUND", "转入退款"),

                            NOTPAY("NOTPAY", "未支付"),

                            CLOSED("CLOSED", "已关闭"),

                            REVOKED("REVOKED", "已撤销（仅付款码支付会返回）"),

                            USERPAYING("USERPAYING", "用户支付中（仅付款码支付会返回）"),

                            PAYERROR("PAYERROR", "支付失败（仅付款码支付会返回）");

        public final String value;

        public final String desc;

        TradeState(String value, String desc) {
            this.value = value;
            this.desc = desc;
        }
    }

}
