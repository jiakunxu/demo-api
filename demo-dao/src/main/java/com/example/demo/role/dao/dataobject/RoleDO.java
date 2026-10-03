package com.example.demo.role.dao.dataobject;

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
@TableName("tb_role")
public class RoleDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = -4826275632663101733L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

    private String            code;

    /**
     * 名称
     */
    private String            name;

    /**
     * 备注
     */
    private String            remark;

    @TableField("`order`")
    private Integer           order;

    /**
     * 状态
     */
    private String            status;

    public RoleDO(BigInteger id) {
        this.id = id;
    }

}
