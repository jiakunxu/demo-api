package com.example.demo.chat.dao.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("tb_chat_detail")
public class ChatDetailDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 4553378337957619071L;

    @TableId(type = IdType.AUTO)
    private BigInteger        id;

    private String            chatId;

    private BigInteger        userId;

    private BigInteger        friendId;

    /**
     * 我 me
     * 你 you
     */
    @TableField("`from`")
    private String            from;

    private String            type;

    private String            content;

}
