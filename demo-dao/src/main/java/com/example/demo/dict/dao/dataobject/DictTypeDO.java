package com.example.demo.dict.dao.dataobject;

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
@TableName("tb_dict_type")
public class DictTypeDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = -3596786743912293542L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

    private String            name;

    @TableField("`value`")
    private String            value;

    private String            remark;

    private String            status;

    public DictTypeDO(String value) {
        this.value = value;
    }

}
