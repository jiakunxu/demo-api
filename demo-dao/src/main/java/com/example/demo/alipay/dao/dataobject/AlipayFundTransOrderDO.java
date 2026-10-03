package com.example.demo.alipay.dao.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.framework.dataobject.BaseDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.math.BigInteger;

@Getter
@Setter
@ToString
@TableName("tb_alipay_fund_trans_order")
public class AlipayFundTransOrderDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 7420423017705642148L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

    /**
     * 商户端的唯一订单号
     */
    private String            outBizNo;

    /**
     * 销售产品码，商家和支付宝签约的产品码。
     * std_red_packet：现金红包；
     * trans_account_no_pwd：单笔无密转账
     */
    private String            productCode;

    /**
     * 描述特定的业务场景，如果传递了out_biz_no则该字段为必传。可取的业务场景如下：
     * personal_pay：c2c现金红包-发红包；
     * personal_collection：c2c现金红包-领红包；
     * refund：c2c现金红包-红包退回；
     * direct_transfer：b2c现金红包、单笔无密转账
     */
    private String            bizScene;

    /**
     * 请求来源的接口
     */
    private String            originInterface;

    /**
     * 支付宝支付资金流水号，仅当转账成功才会返回该参数
     */
    private String            payFundOrderId;

    /**
     * 支付宝转账单据号
     */
    private String            orderId;

    /**
     * 转账单据状态。可能出现的状态如下：
     * success：转账成功；
     * wait_pay：等待支付；
     * closed：订单超时关闭
     *
     * alipay.fund.trans.app.pay涉及的状态： wait_pay success closed
     * alipay.fund.trans.uni.transfer、alipay.fund.trans.refund涉及的状态：success
     */
    private String            status;

    /**
     * 资金订单的操作类型，
     * create-创建；
     * finish- 订单处理已完结
     * close-超时关闭 ；
     */
    private String            actionType;

    /**
     * 转账金额
     */
    private String            transAmount;

    /**
     * 支付完成时间
     */
    private String            payDate;

    /**
     * 自动退款时间
     */
    private String            refundDate;

    /**
     * 无忧收场景下的受理单号
     */
    private String            entrustOrderId;

    /**
     * 失败子单具体状态
     */
    private String            subOrderStatus;

    /**
     * 特殊场景提供，当子单出现异常导致主单失败或者退款时，会提供此字段，用于透出子单具体的错误场景
     */
    private String            subOrderErrorCode;

    /**
     * 特殊场景提供，当子单出现异常导致主单失败或者退款时，会提供此字段，用于透出子单具体的错误场景
     */
    private String            subOrderFailReason;

}
