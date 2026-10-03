package com.example.demo.dingtalk.dao.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.framework.dataobject.BaseDO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.math.BigInteger;

/**
 * @author JiakunXu
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@TableName("tb_dingtalk_agent")
public class DingtalkAgentDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = -3085683708399250037L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

    private String            corpId;

    private String            agentId;

    private String            appKey;

    private String            appSecret;

}
