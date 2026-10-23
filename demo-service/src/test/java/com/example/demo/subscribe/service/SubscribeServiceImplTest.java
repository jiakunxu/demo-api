package com.example.demo.subscribe.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.subscribe.api.bo.Subscribe;
import com.example.demo.subscribe.dao.dataobject.SubscribeDO;
import com.example.demo.subscribe.dao.mapper.SubscribeMapper;
import com.example.demo.tunnel.dao.dataobject.TunnelDO;
import com.example.demo.mq.api.ProducerService;
import com.example.demo.socket.api.bo.Message;
import com.example.demo.support.ServiceTestSupport;
import org.mockito.ArgumentCaptor;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigInteger;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscribeServiceImplTest {
    private final SubscribeMapper mapper = mock(SubscribeMapper.class);
    private final ProducerService producer = mock(ProducerService.class);
    private final SubscribeServiceImpl service = spy(new SubscribeServiceImpl());
    @BeforeEach
    void initialize() { ServiceTestSupport.mapper(service, mapper, SubscribeDO.class);
    ReflectionTestUtils.setField(service, "producerService", producer);
    }

    @Test
    void countOverloadsUseCorrectMapperAndLongValues() {
        when(mapper.count0(any())).thenReturn(3000000000L);
        when(mapper.count1(any())).thenReturn(3000000001L);
        assertThat(service.countSubscribe(BigInteger.ONE, "client", "app", "1")).isEqualTo(3000000000L);
        assertThat(service.countSubscribe(BigInteger.ONE, "app", "1")).isEqualTo(3000000001L);
        assertThat(service.countSubscribe(null, "app", "1")).isZero();
        assertThat(service.countSubscribe(null, "client", "app", "1")).isZero();
    }

    @Test
    void existingSubscriptionAvoidsDuplicateInsert() {
        SubscribeDO row = new SubscribeDO(); row.setId(BigInteger.TEN);
        doReturn(row).when(service).getOne(any(Wrapper.class));
        assertThat(service.insertSubscribe(BigInteger.ONE, "client", "app", "999").getId()).isEqualTo(BigInteger.TEN);
        ArgumentCaptor<Wrapper<SubscribeDO>> capture = ArgumentCaptor.captor();
        verify(service).getOne(capture.capture());
        var query = (LambdaQueryWrapper<SubscribeDO>) capture.getValue();
        assertThat(query.getSqlSegment()).contains("user_id =", "app_id =", "scene =", "scene_id =")
            .endsWith("LIMIT 1").doesNotContain("ORDER BY");
        assertThat(query.getParamNameValuePairs().values())
            .containsExactlyInAnyOrder(BigInteger.ONE, "client", "app", BigInteger.ONE);
        assertThat(query.getSqlSelect()).isNull();
        verify(service, never()).save(any(SubscribeDO.class));
    }

    @Test
    void newSubscriptionDefaultsMissingSceneIdAndReturnsGeneratedId() {
        doReturn(null).when(service).getOne(any(Wrapper.class));
        doAnswer(invocation -> { SubscribeDO row = invocation.getArgument(0); assertThat(row.getSceneId()).isEqualTo(BigInteger.ZERO); assertThat(row.getCreateBy()).isEqualTo("1"); row.setId(BigInteger.TEN); return true; }).when(service).save(any(SubscribeDO.class));
        assertThat(service.insertSubscribe(BigInteger.ONE, "client", "chat", null).getId()).isEqualTo(BigInteger.TEN);
        assertThatThrownBy(() -> service.insertSubscribe(null, "client", "app", null)).hasMessage("参数信息不能为空");
    }

    @Test
    void missingSubscriptionReturnsNull() {
        doReturn(null).when(service).getOne(any(Wrapper.class));
        assertThat(service.getSubscribe(BigInteger.TEN, "client", "chat", "99")).isNull();
        ArgumentCaptor<Wrapper<SubscribeDO>> capture = ArgumentCaptor.captor();
        verify(service).getOne(capture.capture());
        var query = (LambdaQueryWrapper<SubscribeDO>) capture.getValue();
        query.getSqlSegment();
        assertThat(query.getParamNameValuePairs().values())
            .containsExactlyInAnyOrder(BigInteger.TEN, "client", "chat", BigInteger.valueOf(99));
        verifyNoInteractions(producer);
    }

    @Test
    void invalidLookupArgumentsAvoidPersistence() {
        assertThat(service.getSubscribe(BigInteger.ONE, " ", "app", "1")).isNull();
        assertThat(service.getSubscribe(BigInteger.ONE, "client", " ", "1")).isNull();
        assertThat(service.getSubscribe(BigInteger.ONE, "client", "app", " ")).isNull();
        assertThatThrownBy(() -> service.getSubscribe(BigInteger.ONE, "client", "app", "invalid"))
            .isInstanceOf(NumberFormatException.class);
        verify(service, never()).getOne(any(Wrapper.class));
        verifyNoInteractions(mapper, producer);
    }

    @Test
    void publishesAcrossFullAndPartialPagesThenStops() {
        List<TunnelDO> first = IntStream.range(0, 20).mapToObj(i -> { TunnelDO tunnel = new TunnelDO(); tunnel.setTunnelId("tunnel-" + i); return tunnel; }).toList();
        TunnelDO last = new TunnelDO(); last.setTunnelId("last");
        when(mapper.list(any())).thenReturn(first, List.of(last));
        service.sendMessage("app", "1", new Message("event", "payload"));
        verify(producer, times(21)).send(eq("topic"), eq("web.socket"), any(byte[].class), anyString());
        verify(producer).send(eq("topic"), eq("web.socket"), any(byte[].class), eq("last"));
        var queries = ArgumentCaptor.forClass(SubscribeDO.class);
        verify(mapper, times(2)).list(queries.capture());
        assertThat(queries.getAllValues()).extracting(SubscribeDO::getPageNo).containsExactly(1L, 2L);
        assertThat(queries.getAllValues()).extracting(SubscribeDO::getPageSize).containsOnly(20L);
    }

    @Test
    void invalidQueriesAndEmptyPageDoNotPublish() {
        assertThat(service.getSubscribe(null, "client", "app", "1")).isNull();
        assertThat(service.listSubscribes("", "1", new Subscribe())).isEmpty();
        service.sendMessage("", "1", new Message("event", "data")); verifyNoInteractions(mapper, producer);
        service.sendMessage("app", "1", new Message("event", "data")); verifyNoInteractions(producer);
    }
}
