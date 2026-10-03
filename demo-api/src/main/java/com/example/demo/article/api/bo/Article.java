package com.example.demo.article.api.bo;

import com.example.demo.framework.bo.BaseBO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
public class Article extends BaseBO {

    @Serial
    private static final long serialVersionUID = 6414172869018628700L;

    private BigInteger        id;

    public Article(BigInteger id) {
        this.id = id;
    }

}
