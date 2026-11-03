package com.example.demo.login.dao.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.framework.dataobject.BaseDO;
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
@TableName("tb_login_log")
public class LoginLogDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = -4810978561182544347L;

    @TableId(type = IdType.AUTO)
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

    public LoginLogDO(BigInteger id) {
        this.id = id;
    }

}
