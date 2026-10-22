package com.example.demo.framework.dataobject;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
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

    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String            createBy;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date              createTime;

    @TableField(insertStrategy = FieldStrategy.NEVER)
    private String            updateBy;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date              updateTime;

}
