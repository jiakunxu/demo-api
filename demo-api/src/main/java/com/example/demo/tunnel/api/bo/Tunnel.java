package com.example.demo.tunnel.api.bo;

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
public class Tunnel extends BaseBO {

    @Serial
    private static final long serialVersionUID = -2952991612958654453L;

    private BigInteger        id;

    private BigInteger        userId;

    private String            tunnelId;

    private String            host;

    public Tunnel(BigInteger id) {
        this.id = id;
    }

    public Tunnel(String tunnelId) {
        this.tunnelId = tunnelId;
    }

}
