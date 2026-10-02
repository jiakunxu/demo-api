package com.example.demo.operate.dao.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.framework.dataobject.BaseDO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.math.BigInteger;
import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
@TableName("tb_operate_log")
public class OperateLogDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = -5521798017790714164L;

    private BigInteger        id;

    /**
     * 操作人
     */
    private String            operator;

    /**
     * 操作模块
     */
    private String            module;

    /**
     * 操作类型
     */
    private String            operateType;

    /**
     * 单据编号
     */
    private String            orderNo;

    /**
     * 操作内容
     */
    private String            content;

    /**
     * 操作时间
     */
    private Date              operateTime;

    private String            clazz;

    /**
     * 请求uri
     */
    private String            requestUri;

    /**
     * 请求方式
     */
    private String            requestMethod;

    /**
     * 请求参数
     */
    private String            requestParams;

    /**
     * 主机地址
     */
    private String            ip;

    /**
     * ip_addr
     */
    private String            ipAddr;

    /**
     * 状态
     */
    private String            status;

    /**
     * 错误消息
     */
    private String            errMsg;

    public OperateLogDO(BigInteger id) {
        this.id = id;
    }

}
