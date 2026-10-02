package com.example.demo.config.dao.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("tb_config")
public class ConfigDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = -6484949074708858420L;

    @TableId(type = IdType.AUTO)
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
    private String            remark;

    @TableField("is_system")
    private Boolean           system;

}
