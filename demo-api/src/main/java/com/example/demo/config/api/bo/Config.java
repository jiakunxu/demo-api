package com.example.demo.config.api.bo;

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
public class Config extends BaseBO {

    @Serial
    private static final long serialVersionUID = 6796833802963843729L;

    private BigInteger        id;

    /**
     * 名称
     */
    private String            name;

    private String            key;

    private String            value;

    /**
     * 备注
     */
    private String            remark;

    private Boolean           system;

}
