package com.example.demo.framework.bo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author JiakunXu
 */
@Getter
@Setter
public class BaseBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6425935261415820282L;

    private String            search;

    private String            start;

    private String            end;

    private String[]          dateRange;

    /**
     * @see com.example.demo.framework.config.JsonConfigurer
     */
    private String[]          codes;

    /**
     * 排序字段.
     */
    private String            sort;

    /**
     * 排序类型.
     */
    private String            dir;

    /**
     * 分页号，从1开始.
     */
    private Long              pageNo;

    private Long              pageSize;

    private Date              createTime;

    private Date              updateTime;

}
