package com.example.demo.user.dao.dataobject;

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
@TableName("tb_user_role")
public class UserRoleDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = -5384472265511972841L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

    /**
     * 员工
     */
    private BigInteger        userId;

    /**
     * 角色
     */
    private BigInteger        roleId;

    @TableField(exist = false)
    private BigInteger[]      userIds;

    /**
     * role.code
     */
    @TableField(exist = false)
    private String            code;

    /**
     * role.status
     */
    @TableField(exist = false)
    private String            status;

}
