package com.example.demo.chat.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.chat.api.ChatDetailService;
import com.example.demo.chat.api.bo.Chat;
import com.example.demo.chat.api.bo.ChatDetail;
import com.example.demo.chat.dao.dataobject.ChatDetailDO;
import com.example.demo.chat.dao.mapper.ChatDetailMapper;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.mq.api.ProducerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author JiakunXu
 */
@Slf4j
@Service
public class ChatDetailServiceImpl extends ServiceImpl<ChatDetailMapper, ChatDetailDO>
                                   implements ChatDetailService {

    @Autowired
    private ProducerService producerService;

    @Override
    public List<ChatDetail> listChatDetails(BigInteger userId, String id, String friendId,
                                            String pageNo, String pageSize) {
        if (userId == null || StringUtils.isBlank(friendId) || StringUtils.isBlank(pageNo)
            || StringUtils.isBlank(pageSize)) {
            return List.of();
        }

        ChatDetailDO chatDetailDO = new ChatDetailDO();

        if (StringUtils.isNotBlank(id)) {
            chatDetailDO.setId(new BigInteger(id));
        }

        chatDetailDO.setUserId(userId);
        chatDetailDO.setFriendId(new BigInteger(friendId));
        chatDetailDO.setPageNo(Long.valueOf(pageNo));
        chatDetailDO.setPageSize(Long.valueOf(pageSize));

        List<ChatDetail> list = BeanUtil.copy(this.baseMapper.listChatDetails(chatDetailDO),
            ChatDetail.class);

        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }

        return list;
    }

    @Override
    public ChatDetail getChatDetail(BigInteger userId, BigInteger id) {
        if (userId == null || id == null) {
            return null;
        }

        ChatDetailDO chatDetailDO = new ChatDetailDO();
        chatDetailDO.setId(id);
        chatDetailDO.setUserId(userId);

        return BeanUtil.copy(this.baseMapper.getChatDetail(chatDetailDO), ChatDetail.class);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public ChatDetail insertChatDetail(BigInteger userId, BigInteger friendId, String type,
                                       String content) {
        if (userId == null || friendId == null || StringUtils.isBlank(content)) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "参数信息不能为空");
        }

        String chatId = UUID.randomUUID().toString();

        ChatDetailDO chatDetailDO0 = new ChatDetailDO();
        chatDetailDO0.setChatId(chatId);
        chatDetailDO0.setUserId(userId);
        chatDetailDO0.setFriendId(friendId);
        chatDetailDO0.setFrom("me");
        chatDetailDO0.setType(StringUtils.isBlank(type) ? "text" : type);
        chatDetailDO0.setContent(content);
        chatDetailDO0.setCreator(userId.toString());

        this.save(chatDetailDO0);

        ChatDetailDO chatDetailDO1 = new ChatDetailDO();
        chatDetailDO1.setChatId(chatId);
        chatDetailDO1.setUserId(friendId);
        chatDetailDO1.setFriendId(userId);
        chatDetailDO1.setFrom("you");
        chatDetailDO1.setType(StringUtils.isBlank(type) ? "text" : type);
        chatDetailDO1.setContent(content);
        chatDetailDO1.setCreator(userId.toString());

        this.save(chatDetailDO1);

        producerService.send("topic", "chat.message", JSON.toJSONBytes(chatDetailDO1),
            chatDetailDO1.getUserId().toString());

        Date chatTime = new Date();

        Chat chat0 = new Chat();
        chat0.setUserId(chatDetailDO0.getUserId());
        chat0.setFriendId(chatDetailDO0.getFriendId());
        chat0.setChatTime(chatTime);
        chat0.setChatDetailId(chatDetailDO0.getId());
        chat0.setUnread(0);
        producerService.send("topic", "chat.update", JSON.toJSONBytes(chat0), chatId);

        Chat chat1 = new Chat();
        chat1.setUserId(chatDetailDO1.getUserId());
        chat1.setFriendId(chatDetailDO1.getFriendId());
        chat1.setChatTime(chatTime);
        chat1.setChatDetailId(chatDetailDO1.getId());
        chat1.setUnread(1);
        producerService.send("topic", "chat.update", JSON.toJSONBytes(chat1), chatId);

        return BeanUtil.copy(chatDetailDO0, ChatDetail.class);
    }

}
