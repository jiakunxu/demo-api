package com.example.demo.chat.api.bo;

import com.example.demo.user.api.bo.User;
import com.example.demo.framework.bo.BaseBO;
import lombok.*;

import java.io.Serial;
import java.math.BigInteger;
import java.util.Date;

/**
 * @author JiakunXu
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Chat extends BaseBO {

    @Serial
    private static final long serialVersionUID = 1355490942757094850L;

    private BigInteger        id;

    private BigInteger        userId;

    private BigInteger        friendId;

    private Date              chatTime;

    private BigInteger        chatDetailId;

    private Integer           unread;

    // 👇👇👇👇👇 其他 👇👇👇👇👇

    private User              friend;

    private ChatDetail        chatDetail;

    private String            status;

}
