package com.example.demo.chat.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.chat.api.ChatDetailService;
import com.example.demo.chat.api.ChatService;
import com.example.demo.chat.api.ChatStatusService;
import com.example.demo.chat.api.bo.Chat;
import com.example.demo.chat.dao.dataobject.ChatDO;
import com.example.demo.chat.dao.mapper.ChatMapper;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.user.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

/**
 * @author JiakunXu
 */
@Slf4j
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, ChatDO> implements ChatService {

    @Autowired
    private ChatDetailService chatDetailService;

    @Autowired
    private ChatStatusService chatStatusService;

    @Autowired
    private UserService       userService;

    @Override
    public long countChat(BigInteger userId) {
        if (userId == null) {
            return 0;
        }

        ChatDO chatDO = new ChatDO();
        chatDO.setUserId(userId);

        return this.baseMapper.countChat(chatDO);
    }

    @Override
    public List<Chat> listChats(BigInteger userId, String pageNo, String pageSize) {
        if (userId == null || StringUtils.isBlank(pageNo) || StringUtils.isBlank(pageSize)) {
            return List.of();
        }

        ChatDO chatDO = new ChatDO();
        chatDO.setUserId(userId);
        chatDO.setPageNo(Integer.parseInt(pageNo));
        chatDO.setPageSize(Integer.parseInt(pageSize));

        List<Chat> list = BeanUtil.copy(this.baseMapper.listChats(chatDO), Chat.class);

        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }

        for (Chat c : list) {
            c.setFriend(userService.getUser(c.getFriendId()));
            c.setChatDetail(chatDetailService.getChatDetail(userId, c.getChatDetailId()));
        }

        return list;
    }

    @Override
    public Chat getChat(BigInteger userId, String friendId) {
        if (userId == null || StringUtils.isBlank(friendId)) {
            return null;
        }

        Chat chat = new Chat();
        chat.setUserId(userId);
        chat.setFriend(userService.getUser(new BigInteger(friendId)));
        chat.setStatus(chatStatusService.getStatus(userId, friendId));

        return chat;
    }

    @Override
    public Chat getChat(BigInteger userId, BigInteger friendId) {
        if (userId == null || friendId == null) {
            return null;
        }

        ChatDO chatDO = new ChatDO();
        chatDO.setUserId(userId);
        chatDO.setFriendId(friendId);

        return BeanUtil.copy(this.baseMapper.getChat(chatDO), Chat.class);
    }

    @Override
    public Chat saveOrUpdate(BigInteger userId, BigInteger friendId, Chat chat) {
        if (userId == null || friendId == null || chat == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "参数信息不能为空");
        }

        chat.setUserId(userId);
        chat.setFriendId(friendId);

        ChatDO chatDO = BeanUtil.copy(chat, ChatDO.class);
        chatDO.setCreator(userId.toString());
        chatDO.setModifier(userId.toString());

        try {
            if (this.baseMapper.updateChat(chatDO) != 1) {
                this.save(chatDO);
            }
        } catch (Exception e) {
            log.error("{}", chatDO, e);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "信息更新失败，请稍后再试");
        }

        return chat;
    }

    @Override
    public Chat read(BigInteger userId, BigInteger id) {
        if (userId == null || id == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "参数信息不能为空");
        }

        Chat chat = new Chat();
        chat.setId(id);
        chat.setUserId(userId);
        chat.setUnread(0);

        ChatDO chatDO = BeanUtil.copy(chat, ChatDO.class);
        chatDO.setModifier(userId.toString());

        if (this.baseMapper.updateUnread(chatDO) != 1) {
            log.error("{}", chatDO);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "信息不存在");
        }

        return chat;
    }

}
