package com.example.demo.weixin.dao.dataobject;

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
@TableName("tb_weixin_notify")
public class WeixinNotifyDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 852047823813545755L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

}
