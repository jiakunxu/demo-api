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
@TableName("tb_dict_data")
public class DictDataDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 3136596514473209241L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

    private BigInteger        typeId;

    private String            typeValue;

    private String            name;

    private String            value;

    private String            remark;

    private String            status;

    @TableField(exist = false)
    private String[]          typeValues;

    public DictDataDO(BigInteger id) {
        this.id = id;
    }

}
