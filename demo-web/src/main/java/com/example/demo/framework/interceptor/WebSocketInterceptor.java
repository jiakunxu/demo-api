package com.example.demo.framework.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * @author JiakunXu
 */
@Component
public class WebSocketInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, @Nullable Exception exception) {
        if (request instanceof ServletServerHttpRequest
            && response instanceof ServletServerHttpResponse) {
            String name = "Sec-WebSocket-Protocol";
            String header = ((ServletServerHttpRequest) request).getServletRequest()
                .getHeader(name);
            if (StringUtils.isNotBlank(header)) {
                ((ServletServerHttpResponse) response).getServletResponse().addHeader(name, header);
            }
        }
    }

}
