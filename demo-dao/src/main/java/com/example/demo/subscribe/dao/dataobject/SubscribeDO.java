package com.example.demo.subscribe.dao.dataobject;

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
@TableName("tb_subscribe")
public class SubscribeDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 857172631787085965L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

    private BigInteger        userId;

    private String            appId;

    /**
     * 场景.
     */
    private String            scene;

    private BigInteger        sceneId;

}
