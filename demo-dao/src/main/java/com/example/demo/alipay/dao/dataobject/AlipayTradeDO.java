package com.example.demo.alipay.dao.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.framework.dataobject.BaseDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Getter
@Setter
@ToString
@TableName("tb_alipay_trade")
public class AlipayTradeDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1715441966296002388L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

    private String            outTradeNo;

    private String            trade;

}
