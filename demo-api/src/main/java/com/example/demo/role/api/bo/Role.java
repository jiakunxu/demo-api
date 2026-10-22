package com.example.demo.role.api.bo;

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
public class Role extends BaseBO {

    @Serial
    private static final long serialVersionUID = 8843024363081049567L;

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

    private Integer           sortOrder;

    /**
     * 状态
     */
    private String            status;

    private BigInteger[]      menuIds;

    public Role(BigInteger id) {
        this.id = id;
    }

    public Role(BigInteger id, String status) {
        this.id = id;
        this.status = status;
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
