package com.example.demo.mq.manager;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProducerServiceImplTest {

    private static final byte[] BODY = "message content".getBytes(StandardCharsets.UTF_8);
    @Mock
    private RocketMQTemplate template;
    @InjectMocks
    private ProducerServiceImpl service;

    @Test
    void syncSendBuildsMessageAndReturnsId() {
        SendResult result = new SendResult();
        result.setMsgId("message-id");
        when(template.syncSend(eq("topic:tag"), any(Message.class))).thenReturn(result);

        assertThat(service.syncSend("topic", "tag", BODY, "business-key")).isEqualTo("message-id");

        ArgumentCaptor<Message<?>> message = messageCaptor();
        verify(template).syncSend(eq("topic:tag"), message.capture());
        assertMessage(message.getValue());
    }

    @Test
    void delayedSendUsesSecondsAndReturnsId() {
        SendResult result = new SendResult();
        result.setMsgId("delayed-id");
        when(template.syncSendDelayTimeSeconds(eq("topic:tag"), any(Message.class), eq(30L)))
            .thenReturn(result);

        assertThat(service.syncSend("topic", "tag", BODY, "business-key", 30)).isEqualTo("delayed-id");

        ArgumentCaptor<Message<?>> message = messageCaptor();
        verify(template).syncSendDelayTimeSeconds(eq("topic:tag"), message.capture(), eq(30L));
        assertMessage(message.getValue());
    }

    @Test
    void sendBuildsMessage() {
        service.send("topic", "tag", BODY, "business-key");

        ArgumentCaptor<Message<?>> message = messageCaptor();
        verify(template).send(eq("topic:tag"), message.capture());
        assertMessage(message.getValue());
    }

    @Test
    void syncSendPreservesFailureCause() {
        RuntimeException failure = new RuntimeException("broker unavailable");
        when(template.syncSend(eq("topic:tag"), any(Message.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.syncSend("topic", "tag", BODY, "business-key"))
            .isInstanceOf(RuntimeException.class).hasMessage("broker unavailable").hasCause(failure);
    }

    @Test
    void delayedSendPreservesFailureCause() {
        RuntimeException failure = new RuntimeException("broker unavailable");
        when(template.syncSendDelayTimeSeconds(eq("topic:tag"), any(Message.class), eq(30L)))
            .thenThrow(failure);

        assertThatThrownBy(() -> service.syncSend("topic", "tag", BODY, "business-key", 30))
            .isInstanceOf(RuntimeException.class).hasCause(failure);
    }

    @Test
    void sendCurrentlyDoesNotPropagateFailure() {
        doThrow(new RuntimeException("broker unavailable")).when(template)
            .send(eq("topic:tag"), any(Message.class));

        // Characterizes the current best-effort API, not a guarantee of delivery.
        assertThatCode(() -> service.send("topic", "tag", BODY, "business-key"))
            .doesNotThrowAnyException();
    }

    private static void assertMessage(Message<?> message) {
        assertThat(message.getPayload()).isEqualTo(BODY);
        assertThat(message.getHeaders().get(RocketMQHeaders.KEYS)).isEqualTo("business-key");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static ArgumentCaptor<Message<?>> messageCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(Message.class);
    }
}
