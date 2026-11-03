package com.example.demo.dict.api.bo;

import com.example.demo.framework.bo.BaseBO;
import lombok.*;

import java.io.Serial;
import java.math.BigInteger;

/**
 * @author JiakunXu
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DictData extends BaseBO {

    @Serial
    private static final long serialVersionUID = -8188973827667833073L;

    private BigInteger        id;

    private BigInteger        typeId;

    private String            typeValue;

    private String            name;

    private String            value;

    private String            remark;

    private Integer           sortOrder;

    private String            status;

    private String[]          typeValues;

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
