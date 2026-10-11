package com.example.demo.weixin.manager;

import com.alibaba.fastjson2.JSONException;
import com.example.demo.framework.util.HttpUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class Code2SessionServiceImplTest {

    private static final String URL = "https://api.weixin.qq.com/sns/jscode2session?grant_type=authorization_code&appid=test-app&secret=test-secret&js_code=test-code";
    private final Code2SessionServiceImpl service = new Code2SessionServiceImpl();

    @Test
    void buildsRequestAndParsesSession() {
        try (var http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(URL)).thenReturn(
                "{\"openid\":\"open-id\",\"session_key\":\"session-key\",\"unionid\":\"union-id\"}");

            var session = service.getSession("test-app", "test-secret", "test-code");

            assertThat(session.getOpenid()).isEqualTo("open-id");
            assertThat(session.getSessionKey()).isEqualTo("session-key");
            assertThat(session.getUnionid()).isEqualTo("union-id");
            http.verify(() -> HttpUtil.get(URL));
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "null")
    void rejectsEmptyResponse(String response) {
        try (var http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(URL)).thenReturn(response);
            assertThatThrownBy(() -> service.getSession("test-app", "test-secret", "test-code"))
                .isInstanceOf(RuntimeException.class).hasMessage("session is null.");
        }
    }

    @Test
    void rejectsWechatError() {
        try (var http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(URL)).thenReturn("{\"errcode\":40029,\"errmsg\":\"invalid code\"}");
            assertThatThrownBy(() -> service.getSession("test-app", "test-secret", "test-code"))
                .isInstanceOf(RuntimeException.class).hasMessage("invalid code");
        }
    }

    @Test
    void preservesParseFailureCause() {
        try (var http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(URL)).thenReturn("not-json");
            assertThatThrownBy(() -> service.getSession("test-app", "test-secret", "test-code"))
                .isInstanceOf(RuntimeException.class).hasCauseInstanceOf(JSONException.class);
        }
    }

    @Test
    void preservesHttpFailureCause() {
        RuntimeException failure = new RuntimeException("connection failed");
        try (var http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(URL)).thenThrow(failure);
            assertThatThrownBy(() -> service.getSession("test-app", "test-secret", "test-code"))
                .isInstanceOf(RuntimeException.class).hasCause(failure);
        }
    }
}
