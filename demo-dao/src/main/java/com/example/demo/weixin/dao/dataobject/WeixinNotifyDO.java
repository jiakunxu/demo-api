package com.example.demo.weixin.dao.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.framework.dataobject.BaseDO;
import lombok.*;

import java.io.Serial;
import java.math.BigInteger;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_weixin_notify")
public class WeixinNotifyDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 852047823813545755L;

    @TableId
    private BigInteger        id;

}
