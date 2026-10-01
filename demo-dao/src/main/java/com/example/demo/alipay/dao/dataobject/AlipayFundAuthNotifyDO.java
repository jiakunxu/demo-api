package com.example.demo.alipay.dao.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.framework.dataobject.BaseDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.Date;

@Getter
@Setter
@ToString
@TableName("tb_alipay_fund_auth_notify")
public class AlipayFundAuthNotifyDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 3840201379711939631L;

    private String            notifyId;

    private Date              notifyTime;

    /**
     * 支付宝资金授权订单号。
     */
    private String            authNo;

    /**
     * 通知类型。
     */
    private String            notifyType;

    /**
     * 商家的资金授权订单号。
     */
    private String            outOrderNo;

    /**
     * 支付宝的资金操作流水号。
     */
    private String            operationId;

    /**
     * 商家资金操作流水号。
     */
    private String            outRequestNo;

    /**
     * 资金操作类型，枚举值如下：
     *
     * freeze：冻结。
     * unfreeze：解冻。
     * pay：转交易。
     */
    private String            operationType;

    /**
     * 本次操作冻结的金额。
     *
     * 单位为：元（人民币），精确到小数点后两位。
     */
    private String            amount;

    /**
     * 资金预授权明细的状态，枚举值如下：
     *
     * init：初始。
     * success: 该明细操作成功。
     * closed：该明细操作已关闭。
     */
    private String            status;

    /**
     * 明细创建时间。
     */
    private String            gmtCreate;

    /**
     * 明细处理完成时间。
     */
    private String            gmtTrans;

    /**
     * 付款方支付宝账号登录号，脱敏。
     */
    private String            payerLogonId;

    /**
     * 付款方支付宝账号 uid。
     */
    private String            payerUserId;

    /**
     * 收款方支付宝账号，脱敏。
     */
    private String            payeeLogonId;

    /**
     * 收款方支付宝账号 uid。
     */
    private String            payeeUserId;

    /**
     * 累计冻结金额。
     */
    private String            totalFreezeAmount;

    /**
     * 累计解冻金额。
     */
    private String            totalUnfreezeAmount;

    /**
     * 累计支付金额。
     */
    private String            totalPayAmount;

    /**
     * 剩余冻结金额。
     */
    private String            restAmount;

    /**
     * 本次操作中信用金额，单位为：元（人民币），精确到小数点后两位。
     */
    private String            creditAmount;

    /**
     * 本次操作中自有资金金额，单位为：元（人民币），精确到小数点后两位。
     */
    private String            fundAmount;

    /**
     * 累计冻结信用金额，单位为：元（人民币），精确到小数点后两位。
     */
    private String            totalFreezeCreditAmount;

    /**
     * 累计冻结自有资金金额，单位为：元（人民币），精确到小数点后两位。
     */
    private String            totalFreezeFundAmount;

    /**
     * 累计解冻信用金额，单位为：元（人民币），精确到小数点后两位。
     */
    private String            totalUnfreezeCreditAmount;

    /**
     * 累计解冻自有资金金额，单位为：元（人民币），精确到小数点后两位。
     */
    private String            totalUnfreezeFundAmount;

    /**
     * 累计支付信用金额，单位为：元（人民币），精确到小数点后两位。
     */
    private String            totalPayCreditAmount;

    /**
     * 累计支付自有资金金额，单位为：元（人民币），精确到小数点后两位。
     */
    private String            totalPayFundAmount;

    /**
     * 剩余冻结信用金额，单位为：元（人民币），精确到小数点后两位。
     */
    private String            restCreditAmount;

    /**
     * 剩余冻结自有资金金额，单位为：元（人民币），精确到小数点后两位。
     */
    private String            restFundAmount;

    /**
     * 预授权类型，目前支持 credit_auth(信用预授权)
     */
    private String            preAuthType;

    /**
     * 芝麻透出给商家的信息，具体内容由商家与芝麻约定后返回。
     */
    private String            creditMerchantExt;

    private String            charset;

    private String            sign;

    private String            signType;

    private String            version;

}
