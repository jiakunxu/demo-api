package com.example.demo.corp.api.bo;

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
public class Corp extends BaseBO {

    @Serial
    private static final long serialVersionUID = 7032789874318590836L;

    private BigInteger        id;

    private String            name;

}
