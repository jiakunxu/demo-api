package com.example.demo.operate.api.bo;

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
public class OperateLog extends BaseBO {

    @Serial
    private static final long serialVersionUID = -4732081383657654965L;

    private BigInteger        id;

    /**
     * 操作人
     */
    private String            operator;

    /**
     * 操作模块
     */
    private String            module;

    /**
     * 操作类型
     */
    private String            operateType;

    /**
     * 操作内容
     */
    private String            content;

    /**
     * 操作时间
     */
    private Date              operateTime;

    private String            clazz;

    /**
     * 请求uri
     */
    private String            requestUri;

    /**
     * 请求方式
     */
    private String            requestMethod;

    /**
     * 请求参数
     */
    private String            requestParams;

    /**
     * 主机地址
     */
    private String            ip;

    /**
     * ip_addr
     */
    private String            ipAddr;

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
