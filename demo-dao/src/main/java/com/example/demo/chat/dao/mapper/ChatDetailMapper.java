package com.example.demo.chat.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.chat.dao.dataobject.ChatDetailDO;

import java.util.List;

/**
 * @author JiakunXu
 */
public interface ChatDetailMapper extends BaseMapper<ChatDetailDO> {

    List<ChatDetailDO> listChatDetails(ChatDetailDO chatDetailDO);

    ChatDetailDO getChatDetail(ChatDetailDO chatDetailDO);

}
