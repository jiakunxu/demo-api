package com.example.demo.socket.manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketManagerTest {

    private final String key = UUID.randomUUID().toString();
    @Mock
    private WebSocketSession session;

    @AfterEach
    void cleanUp() {
        WebSocketManager.remove(key);
    }

    @Test
    void registeredSessionCanBeRetrieved() {
        WebSocketManager.put(key, session);
        assertThat(WebSocketManager.get(key)).isSameAs(session);
    }

    @Test
    void removingMissingSessionIsSafe() {
        assertThatCode(() -> WebSocketManager.remove(key)).doesNotThrowAnyException();
        assertThat(WebSocketManager.get(key)).isNull();
        verifyNoInteractions(session);
    }

    @Test
    void removalClosesOpenSessionOnlyOnce() throws IOException {
        WebSocketManager.put(key, session);
        when(session.isOpen()).thenReturn(true);

        WebSocketManager.remove(key);
        WebSocketManager.remove(key);

        assertThat(WebSocketManager.get(key)).isNull();
        verify(session, times(1)).close();
    }

    @Test
    void removalDoesNotCloseAlreadyClosedSession() throws IOException {
        WebSocketManager.put(key, session);
        when(session.isOpen()).thenReturn(false);

        WebSocketManager.remove(key);

        assertThat(WebSocketManager.get(key)).isNull();
        verify(session, never()).close();
    }

    @Test
    void failedCloseStillRemovesSession() throws IOException {
        WebSocketManager.put(key, session);
        when(session.isOpen()).thenReturn(true);
        doThrow(new IOException("close failed")).when(session).close();

        assertThatCode(() -> WebSocketManager.remove(key)).doesNotThrowAnyException();
        assertThat(WebSocketManager.get(key)).isNull();
        verify(session).close();
    }
}
