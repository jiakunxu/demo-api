package com.example.demo.socket.api;

/**
 * @author JiakunXu
 */
public interface WebSocketService {

    void close(String tunnelId);

    /**
     *
     * @param tunnelId
     * @param message
     */
    void sendMessage(String tunnelId, String message);

}
