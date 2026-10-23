package com.example.demo.dict.api.bo;

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
public class DictType extends BaseBO {

    @Serial
    private static final long serialVersionUID = -1987286249400648943L;

    private BigInteger        id;

    private String            name;

    private String            value;

    private String            remark;

    private String            status;

    public DictType(BigInteger id) {
        this.id = id;
    }

}
