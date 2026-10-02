package com.example.demo.wxpay.dao.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@TableName("tb_wxpay_profit_sharing")
public class WxpayProfitSharingDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2321748702649952690L;

    /**
     * 微信分账单号
     */
    private String            orderId;

    private String            mchid;

    private String            spMchid;

    /**
     * 子商户号
     */
    private String            subMchid;

    /**
     * 微信订单号
     */
    private String            transactionId;

    /**
     * 商户分账单号
     */
    private String            outOrderNo;

    /**
     * 分账单状态
     */
    private String            state;

    /**
     * 分账接收方列表
     */
    private String            receivers;

    private String            successTime;

}
