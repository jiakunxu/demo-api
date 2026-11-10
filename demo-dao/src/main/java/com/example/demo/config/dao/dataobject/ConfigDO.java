package com.example.demo.config.dao.dataobject;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("tb_config")
public class ConfigDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = -6484949074708858420L;

    @TableId
    private BigInteger        id;

    /**
     * 名称
     */
    private String            name;

    @TableField("`key`")
    private String            key;

    @TableField("`value`")
    private String            value;

    /**
     * 备注
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String            remark;

    @TableField("is_system")
    private Boolean           system;

}
