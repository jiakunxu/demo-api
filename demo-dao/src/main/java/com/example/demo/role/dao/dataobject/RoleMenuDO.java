package com.example.demo.role.dao.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.framework.dataobject.BaseDO;
import lombok.*;

import java.io.Serial;
import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_role_menu")
public class RoleMenuDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = -5876006806537431915L;

    @TableId
    private BigInteger        id;

    /**
     * 角色
     */
    private BigInteger        roleId;

    /**
     * 菜单
     */
    private BigInteger        menuId;

    @TableField(exist = false)
    private List<BigInteger>  ids;

}
