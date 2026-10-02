package com.example.demo.framework.query;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author JiakunXu
 */
@Getter
@Setter
public class BaseQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = -2047538090603282418L;

    @TableField(exist = false)
    private String            search;

    @TableField(exist = false)
    private String            start;

    @TableField(exist = false)
    private String            end;

    @TableField(exist = false)
    private String[]          codes;

    /**
     * 排序字段.
     */
    @TableField(exist = false)
    private String            sort;

    /**
     * 排序类型.
     */
    @TableField(exist = false)
    private String            dir;

    /**
     * 分页号，从1开始.
     */
    @TableField(exist = false)
    private Long              pageNo;

    @TableField(exist = false)
    private Long              pageSize;

    @TableField(exist = false)
    private Long              offset;

    public Long getOffset() {
        if (this.pageNo == null || this.pageSize == null) {
            return null;
        }
        return (this.pageNo - 1) * this.pageSize;
    }

}
