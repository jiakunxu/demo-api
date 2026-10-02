package com.example.demo.subscribe.api.bo;

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
public class Subscribe extends BaseBO {

    @Serial
    private static final long serialVersionUID = 4067360092344161155L;

    private BigInteger        id;

    private BigInteger        userId;

    private String            appId;

    /**
     * 场景.
     */
    private String            scene;

    private BigInteger        sceneId;

}
