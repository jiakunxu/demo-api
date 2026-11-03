package com.example.demo.user.api.bo;

import com.alibaba.fastjson2.annotation.JSONField;
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
public class User extends BaseBO {

    @Serial
    private static final long serialVersionUID = 4850882151993837337L;

    private BigInteger        id;

    private BigInteger        corpId;

    private String            name;

    private String            username;

    @JSONField(serialize = false)
    private String            password;

    private String            mobile;

    private Boolean           expired;

    private Boolean           locked;

    private Boolean           enabled;

    private String            refreshToken;

    public User(String name) {
        this.name = name;
    }

}
