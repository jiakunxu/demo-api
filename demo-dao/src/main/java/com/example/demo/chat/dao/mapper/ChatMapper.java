package com.example.demo.chat.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.chat.dao.dataobject.ChatDO;

import java.util.List;

/**
 * @author JiakunXu
 */
public interface ChatMapper extends BaseMapper<ChatDO> {

    long countChat(ChatDO chatDO);

    List<ChatDO> listChats(ChatDO chatDO);

    ChatDO getChat(ChatDO chatDO);

    /**
     *
     * @param chatDO
     * @return
     */
    int updateChat(ChatDO chatDO);

    /**
     *
     * @param chatDO
     * @return
     */
    int updateUnread(ChatDO chatDO);

}
