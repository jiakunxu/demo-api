package com.example.demo.chat.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.demo.chat.api.ChatDetailService;
import com.example.demo.chat.api.ChatService;
import com.example.demo.chat.api.ChatStatusService;
import com.example.demo.chat.api.bo.Chat;
import com.example.demo.chat.api.bo.ChatDetail;
import com.example.demo.chat.dao.dataobject.ChatDO;
import com.example.demo.chat.dao.mapper.ChatMapper;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.util.BeanUtil;
import com.example.demo.user.api.UserService;
import com.example.demo.user.api.bo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        return this.getBaseMapper().count(chatDO);
    }

    @Override
    public List<Chat> listChats(BigInteger userId, String pageNo, String pageSize) {
        if (userId == null || StringUtils.isBlank(pageNo) || StringUtils.isBlank(pageSize)) {
            return List.of();
        }

        ChatDO chatDO = new ChatDO();
        chatDO.setUserId(userId);
        chatDO.setPageNo(Long.valueOf(pageNo));
        chatDO.setPageSize(Long.valueOf(pageSize));

        List<Chat> list = BeanUtil.copy(this.getBaseMapper().list(chatDO), Chat.class);

        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }

        Map<BigInteger, User> friendMap = userService
            .listUsers(
                list.stream().map(Chat::getFriendId).filter(Objects::nonNull).distinct().toList())
            .stream().collect(Collectors.toMap(User::getId, Function.identity()));

        Map<BigInteger, ChatDetail> chatDetailMap = chatDetailService
            .listChatDetails(userId,
                list.stream().map(Chat::getChatDetailId).filter(Objects::nonNull).distinct()
                    .toList())
            .stream().collect(Collectors.toMap(ChatDetail::getId, Function.identity()));

        for (Chat item : list) {
            item.setFriend(friendMap.get(item.getFriendId()));
            item.setChatDetail(chatDetailMap.get(item.getChatDetailId()));
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

        var queryWrapper = Wrappers.<ChatDO> lambdaQuery().eq(ChatDO::getUserId, userId)
            .eq(ChatDO::getFriendId, friendId);

        return BeanUtil.copy(this.getOne(queryWrapper), Chat.class);
    }

    @Override
    public Chat saveOrUpdate(BigInteger userId, BigInteger friendId, Chat chat) {
        if (userId == null || friendId == null || chat == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "参数信息不能为空");
        }

        chat.setUserId(userId);
        chat.setFriendId(friendId);

        ChatDO chatDO = BeanUtil.copy(chat, ChatDO.class);
        chatDO.setCreateBy(userId.toString());
        chatDO.setUpdateBy(userId.toString());

        try {
            if (this.getBaseMapper().updateChat(chatDO) != 1) {
                this.save(chatDO);
                chat.setId(chatDO.getId());
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

        var updateWrapper = Wrappers.<ChatDO> lambdaUpdate().eq(ChatDO::getId, id)
            .eq(ChatDO::getUserId, userId).set(ChatDO::getUnread, 0)
            .set(ChatDO::getUpdateBy, userId.toString());

        if (!this.update(updateWrapper)) {
            log.error("{},{}", userId, id);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "信息不存在");
        }

        Chat chat = new Chat();
        chat.setId(id);
        chat.setUserId(userId);
        chat.setUnread(0);

        return chat;
    }

}
