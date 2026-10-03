package com.example.demo.chat.api;

import com.example.demo.chat.api.bo.Chat;

import java.math.BigInteger;
import java.util.List;

/**
 * @author JiakunXu
 */
public interface ChatService {

    /**
     *
     * @param userId
     * @return
     */
    long countChat(BigInteger userId);

    /**
     *
     * @param userId
     * @param pageNo
     * @param pageSize
     * @return
     */
    List<Chat> listChats(BigInteger userId, String pageNo, String pageSize);

    /**
     *
     * @param userId
     * @param friendId
     * @return
     */
    Chat getChat(BigInteger userId, String friendId);

    /**
     *
     * @param userId
     * @param friendId
     * @return
     */
    Chat getChat(BigInteger userId, BigInteger friendId);

    /**
     *
     * @param userId
     * @param friendId
     * @param chat
     * @return
     */
    Chat saveOrUpdate(BigInteger userId, BigInteger friendId, Chat chat);

    /**
     * 已读.
     *
     * @param userId
     * @param id
     * @return
     */
    Chat read(BigInteger userId, BigInteger id);

}
