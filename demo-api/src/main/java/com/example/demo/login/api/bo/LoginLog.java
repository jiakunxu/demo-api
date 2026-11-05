package com.example.demo.login.api.bo;

import com.example.demo.framework.bo.BaseBO;
import lombok.*;

import java.io.Serial;
import java.math.BigInteger;
import java.util.Date;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginLog extends BaseBO {

    @Serial
    private static final long serialVersionUID = -614974035358386189L;

    private BigInteger        id;

    /**
     * 登录账号
     */
    private String            username;

    /**
     * ip
     */
    private String            ip;

    /**
     * 登录地址
     */
    private String            ipAddr;

    /**
     * 登录时间
     */
    private Date              loginTime;

    /**
     * 状态
     */
    private String            status;

    /**
     * 错误消息
     */
    private String            errMsg;

    public enum Status {
                        /**
                         * 成功/失败
                         */
                        SUCCESS("S", "成功"), FAIL("F", "失败");

        public final String value;

        public final String desc;

        Status(String value, String desc) {
            this.value = value;
            this.desc = desc;
        }
    }

}
