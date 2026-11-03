package com.example.demo.article.api.bo;

import com.example.demo.framework.bo.BaseBO;
import lombok.*;

import java.io.Serial;
import java.math.BigInteger;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Article extends BaseBO {

    @Serial
    private static final long serialVersionUID = 6414172869018628700L;

    private BigInteger        id;

}
