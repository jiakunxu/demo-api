package com.example.demo.banner.api.bo;

import com.example.demo.framework.bo.BaseBO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
public class Banner extends BaseBO {

    @Serial
    private static final long serialVersionUID = 4337737754426255634L;

    private BigInteger        id;

    public Banner(BigInteger id) {
        this.id = id;
    }

}