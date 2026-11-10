package com.example.demo.user.dao.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.framework.dataobject.BaseDO;
import lombok.*;

import java.io.Serial;
import java.math.BigInteger;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_user")
public class UserDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 852047823813545755L;

    @TableId
    private BigInteger        id;

    private BigInteger        corpId;

    private String            name;

    private String            username;

    private String            password;

    private String            phone;

    @TableField("is_expired")
    private Boolean           expired;

    @TableField("is_locked")
    private Boolean           locked;

    @TableField("is_enabled")
    private Boolean           enabled;

    private String            refreshToken;

    /**
     * user_role
     */
    @TableField(exist = false)
    private BigInteger        roleId;

    @TableField(exist = false)
    private Boolean           exists;

}
