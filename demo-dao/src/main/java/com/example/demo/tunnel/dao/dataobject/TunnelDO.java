package com.example.demo.tunnel.dao.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.framework.dataobject.BaseDO;
import lombok.*;

import java.io.Serial;
import java.math.BigInteger;

/**
 * @author JiakunXu
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_tunnel")
public class TunnelDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 9197256150530836373L;

    @TableId
    private BigInteger        id;

    private BigInteger        userId;

    private String            tunnelId;

    private String            host;

}
