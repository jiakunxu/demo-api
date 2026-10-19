package com.example.demo.chat.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.chat.api.bo.*;
import com.example.demo.chat.dao.dataobject.ChatDetailDO;
import com.example.demo.chat.dao.mapper.ChatDetailMapper;
import com.example.demo.mq.api.ProducerService;
import com.example.demo.support.ServiceTestSupport;
import com.example.demo.support.TransactionProbe;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigInteger;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatDetailServiceImplTest {
    private final ChatDetailMapper mapper = mock(ChatDetailMapper.class);
    private final ProducerService producer = mock(ProducerService.class);
    private final ChatDetailServiceImpl service = spy(new ChatDetailServiceImpl());
    private final BigInteger user = BigInteger.TEN;
    private final BigInteger friend = BigInteger.ONE;
    @BeforeEach
    void initialize() {
        ServiceTestSupport.mapper(service, mapper, ChatDetailDO.class);
        ReflectionTestUtils.setField(service, "producerService", producer);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"image"})
    void messageCreatesPairedRecordsAndPublishesCorrectRecipientAndUnreadCounts(String type) {
        AtomicInteger id = new AtomicInteger(100);
        doAnswer(invocation -> { ChatDetailDO row = invocation.getArgument(0); row.setId(BigInteger.valueOf(id.incrementAndGet())); return true; }).when(service).save(any(ChatDetailDO.class));
        ChatDetail result = service.insertChatDetail(user, friend, type, "hello");
        assertThat(result.getId()).isEqualTo(BigInteger.valueOf(101));
        assertThat(result.getFrom()).isEqualTo("me");
        assertThat(result.getType()).isEqualTo(type == null || type.isEmpty() ? "text" : "image");
        var rows = ArgumentCaptor.forClass(ChatDetailDO.class);
        verify(service, times(2)).save(rows.capture());
        var sent = rows.getAllValues().getFirst(); var received = rows.getAllValues().get(1);
        assertThat(sent.getChatId()).isEqualTo(received.getChatId()).matches("[0-9a-f-]{36}");
        assertThat(received.getUserId()).isEqualTo(friend);
        assertThat(received.getFriendId()).isEqualTo(user);
        assertThat(received.getFrom()).isEqualTo("you");
        assertThat(received.getCreateBy()).isEqualTo("10");
        var payload = ArgumentCaptor.forClass(byte[].class);
        verify(producer).send(eq("topic"), eq("chat.message"), payload.capture(), eq("1"));
        assertThat(JSON.parseObject(payload.getValue(), ChatDetail.class).getId()).isEqualTo(BigInteger.valueOf(102));
        var updates = ArgumentCaptor.forClass(byte[].class);
        verify(producer, times(2)).send(eq("topic"), eq("chat.update"), updates.capture(), eq(sent.getChatId()));
        Chat own = JSON.parseObject(updates.getAllValues().getFirst(), Chat.class);
        Chat other = JSON.parseObject(updates.getAllValues().get(1), Chat.class);
        assertThat(own.getUserId()).isEqualTo(user); assertThat(own.getUnread()).isZero();
        assertThat(own.getChatDetailId()).isEqualTo(BigInteger.valueOf(101));
        assertThat(other.getUserId()).isEqualTo(friend); assertThat(other.getUnread()).isEqualTo(1);
        assertThat(other.getChatDetailId()).isEqualTo(BigInteger.valueOf(102));
        assertThat(own.getChatTime()).isEqualTo(other.getChatTime());
    }

    @Test
    void batchQueryRequiresUserAndMessageIds() {
        assertThat(service.listChatDetails(null, List.of(BigInteger.ONE))).isEmpty();
        assertThat(service.listChatDetails(user, null)).isEmpty();
        assertThat(service.listChatDetails(user, List.of())).isEmpty();
        verifyNoInteractions(mapper, producer);
    }

    @Test
    void batchQueryScopesUserAndIdsAndMapsMessages() {
        List<BigInteger> ids = List.of(BigInteger.ONE, BigInteger.TWO);
        ChatDetailDO row = new ChatDetailDO();
        row.setId(BigInteger.ONE);
        row.setUserId(user);
        row.setContent("hello");
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(row));

        List<ChatDetail> messages = service.listChatDetails(user, ids);

        assertThat(messages).extracting(ChatDetail::getId).containsExactly(BigInteger.ONE);
        assertThat(messages.getFirst().getContent()).isEqualTo("hello");
        ArgumentCaptor<Wrapper<ChatDetailDO>> capture = ArgumentCaptor.captor();
        verify(mapper).selectList(capture.capture());
        var wrapper = (LambdaQueryWrapper<ChatDetailDO>) capture.getValue();
        assertThat(wrapper.getSqlSegment()).contains("user_id =", "id IN");
        assertThat(wrapper.getParamNameValuePairs().values())
            .containsExactlyInAnyOrder(user, BigInteger.ONE, BigInteger.TWO);
        verifyNoMoreInteractions(mapper);
        verifyNoInteractions(producer);
    }

    @Test
    void batchQueryWithoutMatchesReturnsEmptyList() {
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        assertThat(service.listChatDetails(user, List.of(BigInteger.ONE))).isEmpty();
        verify(mapper).selectList(any(Wrapper.class));
    }

    @Test
    void saveFailureRollsBackAdviceAndPreventsMessagePublication() {
        doThrow(new RuntimeException("database failed")).when(service).save(any(ChatDetailDO.class));
        var transactions = new TransactionProbe();
        assertThatThrownBy(() -> transactions.wrap(service).insertChatDetail(user, friend, "text", "hello")).hasMessage("database failed");
        assertThat(transactions.rollbacks).isEqualTo(1);
        assertThat(transactions.commits).isZero();
        verifyNoInteractions(producer);
    }

    @Test
    void missingParticipantsOrContentAreRejectedBeforeSaving() {
        assertThatThrownBy(() -> service.insertChatDetail(null, friend, "text", "hello")).hasMessage("参数信息不能为空");
        assertThatThrownBy(() -> service.insertChatDetail(user, null, "text", "hello")).hasMessage("参数信息不能为空");
        assertThatThrownBy(() -> service.insertChatDetail(user, friend, "text", " ")).hasMessage("参数信息不能为空");
        verify(service, never()).save(any(ChatDetailDO.class));
        verifyNoInteractions(producer);
    }

    @Test
    void messageQueriesAreScopedToUserFriendAndPagination() {
        ChatDetailDO row = new ChatDetailDO(); row.setId(BigInteger.valueOf(99));
        when(mapper.listChatDetails(any())).thenReturn(List.of(row));
        assertThat(service.listChatDetails(user, "99", "1", "2", "20")).extracting(ChatDetail::getId).containsExactly(row.getId());
        var capture = ArgumentCaptor.forClass(ChatDetailDO.class);
        verify(mapper).listChatDetails(capture.capture());
        assertThat(capture.getValue().getUserId()).isEqualTo(user);
        assertThat(capture.getValue().getFriendId()).isEqualTo(friend);
        assertThat(capture.getValue().getId()).isEqualTo(row.getId());
        assertThat(capture.getValue().getPageNo()).isEqualTo(2L);
        assertThat(capture.getValue().getPageSize()).isEqualTo(20L);
    }

    @Test
    void detailLookupPreservesOwnerScopeAndUsesDefaultProjection() {
        BigInteger id = BigInteger.valueOf(99);
        ChatDetailDO row = new ChatDetailDO();
        row.setId(id);
        row.setUserId(user);
        row.setFrom("me");
        row.setContent("hello");
        doReturn(row).when(service).getOne(any(Wrapper.class));

        ChatDetail result = service.getChatDetail(user, id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getUserId()).isEqualTo(user);
        assertThat(result.getFrom()).isEqualTo("me");
        assertThat(result.getContent()).isEqualTo("hello");
        ArgumentCaptor<Wrapper<ChatDetailDO>> capture = ArgumentCaptor.captor();
        verify(service).getOne(capture.capture());
        var wrapper = (LambdaQueryWrapper<ChatDetailDO>) capture.getValue();
        assertThat(wrapper.getSqlSegment()).contains("id =", "AND", "user_id =")
            .doesNotContain("LIMIT", "ORDER BY");
        assertThat(wrapper.getParamNameValuePairs().values()).containsExactlyInAnyOrder(id, user);
        assertThat(wrapper.getSqlSelect()).isNull();
        verifyNoInteractions(producer);
    }

    @Test
    void missingDetailReturnsNull() {
        doReturn(null).when(service).getOne(any(Wrapper.class));
        assertThat(service.getChatDetail(user, BigInteger.ONE)).isNull();
        verifyNoInteractions(producer);
    }

    @Test
    void invalidQueriesAvoidDatabaseAndNoRowsBecomeEmptyList() {
        assertThat(service.listChatDetails(null, null, "1", "1", "20")).isEmpty();
        assertThat(service.listChatDetails(user, null, " ", "1", "20")).isEmpty();
        assertThat(service.getChatDetail(user, null)).isNull();
        assertThat(service.getChatDetail(null, BigInteger.ONE)).isNull();
        verifyNoInteractions(mapper);
        assertThat(service.listChatDetails(user, null, "1", "1", "20")).isEmpty();
    }
}
