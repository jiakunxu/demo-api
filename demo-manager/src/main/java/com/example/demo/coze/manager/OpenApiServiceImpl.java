package com.example.demo.coze.manager;

import com.alibaba.fastjson2.JSONObject;
import com.example.demo.coze.api.OpenApiService;
import com.example.demo.sse.api.SseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service("com.example.demo.coze.manager.openApiService")
public class OpenApiServiceImpl implements OpenApiService {

    @Autowired
    private SseService sseService;

    @Override
    public void chat(String tunnelId, String botId, String user, String query) {
        JSONObject data = new JSONObject();
        data.put("bot_id", botId);
        data.put("conversation_id", "");
        data.put("user", user);
        data.put("query", query);
        data.put("stream", true);

        WebClient client = WebClient.create("https://api.coze.com");

        client.post().uri("/open_api/v2/chat").contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer ").bodyValue(data.toString())
            .accept(MediaType.TEXT_EVENT_STREAM).retrieve().bodyToFlux(String.class).map(s -> {
                sseService.send(tunnelId, s);
                return s;
            }).doFinally(s -> {
                sseService.complete(tunnelId);
            }).blockLast();
    }

}
