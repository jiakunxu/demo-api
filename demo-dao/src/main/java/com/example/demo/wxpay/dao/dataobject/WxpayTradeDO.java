package com.example.demo.wxpay.dao.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.framework.dataobject.BaseDO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.math.BigInteger;

@Getter
@Setter
@ToString
@NoArgsConstructor
@TableName("tb_wxpay_trade")
public class WxpayTradeDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = -7407102721978781501L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

    private String            outTradeNo;

    private String            trade;

}
