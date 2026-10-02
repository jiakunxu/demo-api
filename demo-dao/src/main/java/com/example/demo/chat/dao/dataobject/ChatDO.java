package com.example.demo.chat.dao.dataobject;

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
import java.util.Date;

/**
 * @author JiakunXu
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@TableName("tb_chat")
public class ChatDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 8356420243427841172L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

    private BigInteger        userId;

    private BigInteger        friendId;

    private Date              chatTime;

    private BigInteger        chatDetailId;

    private Integer           unread;

}
