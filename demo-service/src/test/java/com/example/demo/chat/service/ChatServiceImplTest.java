package com.example.demo.chat.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.chat.api.*;
import com.example.demo.chat.api.bo.*;
import com.example.demo.chat.dao.mapper.ChatMapper;
import com.example.demo.chat.dao.dataobject.ChatDO;
import com.example.demo.user.api.UserService;
import com.example.demo.user.api.bo.User;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.support.ServiceTestSupport;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigInteger;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatServiceImplTest {
    private final ChatMapper mapper = mock(ChatMapper.class);
    private final UserService users = mock(UserService.class);
    private final ChatDetailService details = mock(ChatDetailService.class);
    private final ChatStatusService statuses = mock(ChatStatusService.class);
    private final ChatServiceImpl service = spy(new ChatServiceImpl());
    private final BigInteger user = BigInteger.TEN;
    private final BigInteger friend = BigInteger.ONE;

    @BeforeEach
    void initialize() {
        ServiceTestSupport.mapper(service, mapper, ChatDO.class);
        ReflectionTestUtils.setField(service, "userService", users);
        ReflectionTestUtils.setField(service, "chatDetailService", details);
        ReflectionTestUtils.setField(service, "chatStatusService", statuses);
    }

    @Test
    void updateExistingChatDoesNotInsertOrOverwriteItsId() {
        Chat chat = new Chat(); chat.setId(BigInteger.valueOf(99));
        when(mapper.updateChat(any())).thenReturn(1);
        assertThat(service.saveOrUpdate(user, friend, chat)).isSameAs(chat);
        verify(service, never()).save(any(ChatDO.class));
        assertThat(chat.getId()).isEqualTo(BigInteger.valueOf(99));
        var capture = ArgumentCaptor.forClass(ChatDO.class);
        verify(mapper).updateChat(capture.capture());
        assertThat(capture.getValue().getUserId()).isEqualTo(user);
        assertThat(capture.getValue().getFriendId()).isEqualTo(friend);
        assertThat(capture.getValue().getCreateBy()).isEqualTo("10");
        assertThat(capture.getValue().getUpdateBy()).isEqualTo("10");
    }

    @Test
    void missingChatInsertsAndCopiesGeneratedIdBack() {
        doAnswer(invocation -> { ((ChatDO) invocation.getArgument(0)).setId(BigInteger.valueOf(99)); return true; }).when(service).save(any(ChatDO.class));
        Chat chat = new Chat();
        assertThat(service.saveOrUpdate(user, friend, chat).getId()).isEqualTo(BigInteger.valueOf(99));
        var sequence = inOrder(mapper, service);
        sequence.verify(mapper).updateChat(any());
        sequence.verify(service).save(any(ChatDO.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateOrInsertExceptionBecomesBusinessFailure(boolean updateFailure) {
        RuntimeException failure = new RuntimeException("database failed");
        if (updateFailure) when(mapper.updateChat(any())).thenThrow(failure);
        else doThrow(failure).when(service).save(any(ChatDO.class));
        assertThatThrownBy(() -> service.saveOrUpdate(user, friend, new Chat())).isInstanceOf(ServiceException.class).hasMessage("信息更新失败，请稍后再试");
        if (updateFailure) verify(service, never()).save(any(ChatDO.class));
    }

    @Test
    void missingIdentifiersAreRejectedBeforeDatabaseAccess() {
        assertThatThrownBy(() -> service.saveOrUpdate(null, friend, new Chat())).hasMessage("参数信息不能为空");
        assertThatThrownBy(() -> service.saveOrUpdate(user, null, new Chat())).hasMessage("参数信息不能为空");
        assertThatThrownBy(() -> service.saveOrUpdate(user, friend, null)).hasMessage("参数信息不能为空");
        verifyNoInteractions(mapper);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void readingSetsUnreadToZeroOnlyForRequestedUser(int rows) {
        when(mapper.updateUnread(any())).thenReturn(rows);
        if (rows == 1) assertThat(service.read(user, friend).getUnread()).isZero();
        else assertThatThrownBy(() -> service.read(user, friend)).hasMessage("信息不存在");
        var capture = ArgumentCaptor.forClass(ChatDO.class);
        verify(mapper).updateUnread(capture.capture());
        assertThat(capture.getValue().getId()).isEqualTo(friend);
        assertThat(capture.getValue().getUserId()).isEqualTo(user);
        assertThat(capture.getValue().getUnread()).isZero();
    }

    @Test
    void listConvertsPaginationAndEnrichesFriendAndLatestMessage() {
        ChatDO row = new ChatDO(); row.setFriendId(friend); row.setChatDetailId(BigInteger.valueOf(99));
        when(mapper.list(any())).thenReturn(List.of(row));
        User friendUser = new User(); ChatDetail detail = new ChatDetail();
        friendUser.setId(friend);
        detail.setId(row.getChatDetailId());
        when(users.listUsers(List.of(friend))).thenReturn(List.of(friendUser));
        when(details.listChatDetails(user, List.of(row.getChatDetailId())))
            .thenReturn(List.of(detail));
        List<Chat> result = service.listChats(user, "2", "20");
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getFriend()).isSameAs(friendUser);
        assertThat(result.getFirst().getChatDetail()).isSameAs(detail);
        var capture = ArgumentCaptor.forClass(ChatDO.class);
        verify(mapper).list(capture.capture());
        assertThat(capture.getValue().getPageNo()).isEqualTo(2L);
        assertThat(capture.getValue().getPageSize()).isEqualTo(20L);
        assertThat(capture.getValue().getUserId()).isEqualTo(user);
    }

    @Test
    void batchEnrichmentDeduplicatesIdsAndPreservesChatOrder() {
        ChatDO first = new ChatDO();
        first.setId(BigInteger.valueOf(101));
        first.setFriendId(friend);
        first.setChatDetailId(BigInteger.valueOf(91));
        ChatDO second = new ChatDO();
        second.setId(BigInteger.valueOf(102));
        second.setFriendId(BigInteger.TWO);
        second.setChatDetailId(BigInteger.valueOf(92));
        ChatDO third = new ChatDO();
        third.setId(BigInteger.valueOf(103));
        third.setFriendId(friend);
        third.setChatDetailId(first.getChatDetailId());
        when(mapper.list(any())).thenReturn(List.of(first, second, third));
        User firstFriend = new User(); firstFriend.setId(friend);
        User secondFriend = new User(); secondFriend.setId(BigInteger.TWO);
        ChatDetail firstDetail = new ChatDetail(); firstDetail.setId(first.getChatDetailId());
        ChatDetail secondDetail = new ChatDetail(); secondDetail.setId(second.getChatDetailId());
        List<BigInteger> friendIds = List.of(friend, BigInteger.TWO);
        List<BigInteger> detailIds = List.of(first.getChatDetailId(), second.getChatDetailId());
        when(users.listUsers(friendIds)).thenReturn(List.of(secondFriend, firstFriend));
        when(details.listChatDetails(user, detailIds))
            .thenReturn(List.of(secondDetail, firstDetail));

        List<Chat> result = service.listChats(user, "1", "20");

        assertThat(result).extracting(Chat::getId)
            .containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(result).extracting(Chat::getFriend)
            .containsExactly(firstFriend, secondFriend, firstFriend);
        assertThat(result).extracting(Chat::getChatDetail)
            .containsExactly(firstDetail, secondDetail, firstDetail);
        verify(users).listUsers(friendIds);
        verify(details).listChatDetails(user, detailIds);
        verifyNoMoreInteractions(users, details);
    }

    @Test
    void missingAndNullAssociationsRemainNullAndNullIdsAreNotQueried() {
        ChatDO missing = new ChatDO();
        missing.setFriendId(friend);
        missing.setChatDetailId(BigInteger.valueOf(99));
        when(mapper.list(any())).thenReturn(List.of(missing, new ChatDO()));
        when(users.listUsers(List.of(friend))).thenReturn(List.of());
        when(details.listChatDetails(user, List.of(missing.getChatDetailId())))
            .thenReturn(List.of());

        List<Chat> result = service.listChats(user, "1", "20");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Chat::getFriend).containsOnlyNulls();
        assertThat(result).extracting(Chat::getChatDetail).containsOnlyNulls();
        verify(users).listUsers(List.of(friend));
        verify(details).listChatDetails(user, List.of(missing.getChatDetailId()));
        verifyNoMoreInteractions(users, details);
    }

    @Test
    void emptyAndInvalidQueriesAvoidEnrichment() {
        assertThat(service.countChat(null)).isZero();
        assertThat(service.listChats(null, "1", "20")).isEmpty();
        assertThat(service.listChats(user, "", "20")).isEmpty();
        assertThat(service.getChat(user, (BigInteger) null)).isNull();
        assertThat(service.getChat(null, friend)).isNull();
        assertThat(service.getChat(null, "1")).isNull();
        verifyNoInteractions(mapper, users, details, statuses);
        assertThat(service.listChats(user, "1", "20")).isEmpty();
        verifyNoInteractions(users, details, statuses);
    }

    @Test
    void countUsesLongAndGetByPairIncludesBothIdentifiers() {
        when(mapper.count(any())).thenReturn(3000000000L);
        assertThat(service.countChat(user)).isEqualTo(3000000000L);
        ChatDO row = new ChatDO(); row.setId(BigInteger.valueOf(99));
        doReturn(row).when(service).getOne(any(Wrapper.class));
        assertThat(service.getChat(user, friend).getId()).isEqualTo(row.getId());
        ArgumentCaptor<Wrapper<ChatDO>> capture = ArgumentCaptor.captor();
        verify(service).getOne(capture.capture());
        var wrapper = (LambdaQueryWrapper<ChatDO>) capture.getValue();
        assertThat(wrapper.getSqlSegment()).contains("user_id =", "AND", "friend_id =")
            .doesNotContain("LIMIT", "ORDER BY");
        assertThat(wrapper.getParamNameValuePairs().values())
            .containsExactlyInAnyOrder(user, friend);
        assertThat(wrapper.getSqlSelect()).isNull();
    }

    @Test
    void missingChatReturnsNullWithoutEnrichment() {
        doReturn(null).when(service).getOne(any(Wrapper.class));
        assertThat(service.getChat(user, friend)).isNull();
        verifyNoInteractions(users, details, statuses);
    }

    @Test
    void duplicatePairIsNotSilentlyReducedToOneRow() {
        doThrow(new org.apache.ibatis.exceptions.TooManyResultsException("duplicate pair"))
            .when(service).getOne(any(Wrapper.class));
        assertThatThrownBy(() -> service.getChat(user, friend))
            .isInstanceOf(org.apache.ibatis.exceptions.TooManyResultsException.class);
        verifyNoInteractions(users, details, statuses);
    }

    @Test
    void stringFriendLookupAssemblesUserAndOnlineStatus() {
        User friendUser = new User();
        when(users.getUser(friend)).thenReturn(friendUser);
        when(statuses.getStatus(user, "1")).thenReturn("online");
        Chat chat = service.getChat(user, "1");
        assertThat(chat.getUserId()).isEqualTo(user);
        assertThat(chat.getFriend()).isSameAs(friendUser);
        assertThat(chat.getStatus()).isEqualTo("online");
        verifyNoInteractions(mapper);
    }
}
