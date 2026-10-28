package com.example.demo.socket.manager;

import com.example.demo.socket.api.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * @author JiakunXu
 */
@Slf4j
@Service
public class WebSocketServiceImpl implements WebSocketService {

    @Override
    public void close(String tunnelId) {
        WebSocketSession session = WebSocketManager.get(tunnelId);

        if (session == null) {
            return;
        }

        if (session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                log.error("{}", tunnelId, e);
            }
        }
    }

    @Override
    public void sendMessage(String tunnelId, String message) {
        WebSocketSession session = WebSocketManager.get(tunnelId);

        if (session == null) {
            return;
        }

        if (session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("{},{}", tunnelId, message, e);
            }
        }
    }

}
