package com.example.demo.dict.dao.dataobject;

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
@TableName("tb_dict_type")
public class DictTypeDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = -3596786743912293542L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

    private String            name;

    @TableField("`value`")
    private String            value;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String            remark;

    private String            status;

}
