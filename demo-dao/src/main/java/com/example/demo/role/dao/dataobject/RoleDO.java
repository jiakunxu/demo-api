package com.example.demo.role.dao.dataobject;

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
@TableName("tb_role")
public class RoleDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = -4826275632663101733L;

    @TableId
    private BigInteger        id;

    private String            code;

    /**
     * 名称
     */
    private String            name;

    /**
     * 备注
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String            remark;

    private Integer           sortOrder;

    /**
     * 状态
     */
    private String            status;

}
