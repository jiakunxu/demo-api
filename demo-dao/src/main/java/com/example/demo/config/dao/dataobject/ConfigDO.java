package com.example.demo.config.dao.dataobject;

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

    private BigInteger        id;

    /**
     * 名称
     */
    private String            name;

    private String            key;

    private String            value;

    /**
     * 备注
     */
    private String            remark;

    private Boolean           system;

}
