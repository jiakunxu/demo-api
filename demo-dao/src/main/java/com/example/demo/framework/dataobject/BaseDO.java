package com.example.demo.framework.dataobject;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.example.demo.framework.query.BaseQuery;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Date;

/**
 * @author JiakunXu
 */
@Getter
@Setter
public class BaseDO extends BaseQuery {

    @Serial
    private static final long serialVersionUID = 8461456313695325022L;

    @TableField("is_deleted")
    private Boolean           deleted;

    @JSONField(serialize = false)
    private String            creator;

    @JSONField(serialize = false)
    private String            modifier;

    private Date              createTime;

    private Date              updateTime;

}
