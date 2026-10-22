package com.example.demo.menu.dao.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import com.example.demo.framework.dataobject.BaseDO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@TableName("tb_menu")
public class MenuDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 6834457453642427426L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

    /**
     * 上级
     */
    private BigInteger        pid;

    /**
     * 目录/菜单/按钮
     */
    private String            type;

    /**
     * icon
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String            icon;

    /**
     * 名称
     */
    private String            name;

    /**
     * 路由地址
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String            path;

    /**
     * 组件路径
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String            component;

    /**
     * 路由参数
     */
    @TableField(value = "`query`", updateStrategy = FieldStrategy.ALWAYS)
    private String            query;

    /**
     * 权限
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String            code;

    /**
     * 排序
     */
    private Integer           sortOrder;

    /**
     * 0 不是 1 是
     */
    @TableField("is_external")
    private Boolean           external;

    /**
     * 0 不缓存 1 缓存
     */
    @TableField("is_cached")
    private Boolean           cached;

    /**
     * 0 显示 1 不显示
     */
    @TableField("is_hidden")
    private Boolean           hidden;

    /**
     * 正常 停用
     */
    private String            status;

    @TableField(exist = false)
    private List<BigInteger>  ids;

    @TableField(exist = false)
    private String[]          types;

    /**
     * user user_role role_menu menu.
     */
    @TableField(exist = false)
    private BigInteger        userId;

    @TableField(exist = false)
    private String[]          roleCodes;

    public MenuDO(BigInteger id) {
        this.id = id;
    }

}
