package com.example.demo.corp.dao.dataobject;

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

@Getter
@Setter
@ToString
@NoArgsConstructor
@TableName("tb_corp_role")
public class CorpRoleDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 5088340132394345083L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

}
