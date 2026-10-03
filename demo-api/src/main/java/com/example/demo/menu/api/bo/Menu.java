package com.example.demo.menu.api.bo;

import com.example.demo.framework.bo.BaseBO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.math.BigInteger;

/**
 * @author JiakunXu
 */
@Getter
@Setter
@NoArgsConstructor
public class Menu extends BaseBO {

    @Serial
    private static final long serialVersionUID = 3281112744556210154L;

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
    private String            icon;

    /**
     * 名称
     */
    private String            name;

    /**
     * 路由地址
     */
    private String            path;

    /**
     * 组件路径
     */
    private String            component;

    /**
     * 路由参数
     */
    private String            query;

    /**
     * 权限
     */
    private String            code;

    /**
     * 排序
     */
    private Integer           order;

    /**
     * 0 不是 1 是
     */
    private Boolean           external;

    /**
     * 0 不缓存 1 缓存
     */
    private Boolean           cached;

    /**
     * 0 显示 1 不显示
     */
    private Boolean           hidden;

    /**
     * 正常 停用
     */
    private String            status;

    public Menu(BigInteger id) {
        this.id = id;
    }

    public Boolean getLeaf() {
        return !Menu.Type.CONTENTS.value.equals(this.type) || this.external;
    }

    public enum Type {
                      /**
                       * 目录/菜单/按钮.
                       */
                      CONTENTS("C", "目录"),

                      MENU("M", "菜单"),

                      BUTTON("B", "按钮");

        public final String value;

        public final String desc;

        Type(String value, String desc) {
            this.value = value;
            this.desc = desc;
        }
    }

    public enum Status {
                        /**
                         * 启用/停用
                         */
                        ENABLE("E", "启用"), DISABLE("D", "停用");

        public final String value;

        public final String desc;

        Status(String value, String desc) {
            this.value = value;
            this.desc = desc;
        }
    }

}
