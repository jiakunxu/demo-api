package com.example.demo.socket.manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketServiceImplTest {

    private final String key = UUID.randomUUID().toString();
    private final WebSocketServiceImpl service = new WebSocketServiceImpl();
    @Mock
    private WebSocketSession session;

    @AfterEach
    void cleanUp() {
        WebSocketManager.remove(key);
    }

    @Test
    void missingSessionIsIgnored() {
        assertThatCode(() -> service.sendMessage(key, "hello")).doesNotThrowAnyException();
        verifyNoInteractions(session);
    }

    @Test
    void closedSessionIsNotSentTo() throws IOException {
        WebSocketManager.put(key, session);
        when(session.isOpen()).thenReturn(false);

        service.sendMessage(key, "hello");

        verify(session, never()).sendMessage(any());
    }

    @Test
    void sendsExpectedWireMessage() throws IOException {
        WebSocketManager.put(key, session);
        when(session.isOpen()).thenReturn(true);

        service.sendMessage(key, "hello");

        verify(session).sendMessage(new TextMessage("message:hello"));
    }

    @Test
    void sendFailureDoesNotEscape() throws IOException {
        WebSocketManager.put(key, session);
        when(session.isOpen()).thenReturn(true);
        doThrow(new IOException("connection closed")).when(session).sendMessage(any());

        assertThatCode(() -> service.sendMessage(key, "hello")).doesNotThrowAnyException();
        verify(session).sendMessage(new TextMessage("message:hello"));
    }
}
