package com.example.demo.dingtalk.dao.dataobject;

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
@TableName("tb_dingtalk_notify")
public class DingtalkNotifyDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 6706644591052897288L;

    private BigInteger        id;

}
