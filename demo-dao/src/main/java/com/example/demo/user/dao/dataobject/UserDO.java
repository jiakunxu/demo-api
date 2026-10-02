package com.example.demo.user.dao.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.framework.dataobject.BaseDO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.math.BigInteger;

@Getter
@Setter
@ToString
@NoArgsConstructor
@TableName("tb_user")
public class UserDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 852047823813545755L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

    private BigInteger        corpId;

    private String            name;

    private String            username;

    private String            password;

    private Boolean           expired;

    private Boolean           locked;

    private Boolean           enabled;

    private String            refreshToken;

    /**
     * user_role
     */
    @TableField(exist = false)
    private BigInteger        roleId;

    @TableField(exist = false)
    private Boolean           exists;

    public UserDO(BigInteger id) {
        this.id = id;
    }

    public UserDO(String username) {
        this.username = username;
    }

}
