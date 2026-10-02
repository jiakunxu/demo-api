package com.example.demo.tunnel.dao.dataobject;

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

/**
 * @author JiakunXu
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@TableName("tb_tunnel")
public class TunnelDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 9197256150530836373L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

    private BigInteger        userId;

    private String            tunnelId;

    private String            host;

}
