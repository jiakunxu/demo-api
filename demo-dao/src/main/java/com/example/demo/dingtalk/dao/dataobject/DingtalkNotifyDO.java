package com.example.demo.dingtalk.dao.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.framework.dataobject.BaseDO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

@Getter
@Setter
@ToString
@NoArgsConstructor
@TableName("tb_dingtalk_notify")
public class DingtalkNotifyDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 6706644591052897288L;

}
