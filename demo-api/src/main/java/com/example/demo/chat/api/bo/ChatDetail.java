package com.example.demo.chat.api.bo;

import com.example.demo.framework.bo.BaseBO;
import lombok.*;

import java.io.Serial;
import java.math.BigInteger;

/**
 * @author JiakunXu
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChatDetail extends BaseBO {

    @Serial
    private static final long serialVersionUID = 1355490942757094850L;

    private BigInteger        id;

    private String            chatId;

    private BigInteger        userId;

    private BigInteger        friendId;

    /**
     * 我 me
     * 你 you
     */
    private String            from;

    private String            type;

    private String            content;

}