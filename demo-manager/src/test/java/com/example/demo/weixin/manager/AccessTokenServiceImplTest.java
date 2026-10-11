package com.example.demo.weixin.manager;

import com.alibaba.fastjson2.JSONException;
import com.example.demo.framework.util.HttpUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class AccessTokenServiceImplTest {

    private static final String URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=test-app&secret=test-secret";
    private final AccessTokenServiceImpl service = new AccessTokenServiceImpl();

    @Test
    void buildsRequestAndParsesToken() {
        try (var http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(URL)).thenReturn("{\"access_token\":\"token-value\",\"expires_in\":7200}");

            var token = service.getAccessToken("client_credential", "test-app", "test-secret");

            assertThat(token.getAccessToken()).isEqualTo("token-value");
            assertThat(token.getExpiresIn()).isEqualTo(7200);
            http.verify(() -> HttpUtil.get(URL));
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "null")
    void rejectsEmptyResponse(String response) {
        assertResponseError(response, "access_token is null.");
    }

    @ParameterizedTest
    @ValueSource(strings = { "{}", "{\"access_token\":null}", "{\"access_token\":\"\"}", "{\"access_token\":\" \"}" })
    void rejectsMissingOrBlankToken(String response) {
        assertResponseError(response, "access_token is blank.");
    }

    @Test
    void rejectsWechatErrorBeforeCheckingToken() {
        assertResponseError("{\"errcode\":40013,\"errmsg\":\"invalid appid\"}", "invalid appid");
    }

    @Test
    void preservesParseFailureCause() {
        try (var http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(URL)).thenReturn("not-json");
            assertThatThrownBy(() -> service.getAccessToken("client_credential", "test-app", "test-secret"))
                .isInstanceOf(RuntimeException.class).hasCauseInstanceOf(JSONException.class);
        }
    }

    @Test
    void preservesHttpFailureCause() {
        RuntimeException failure = new RuntimeException("connection failed");
        try (var http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(URL)).thenThrow(failure);
            assertThatThrownBy(() -> service.getAccessToken("client_credential", "test-app", "test-secret"))
                .isInstanceOf(RuntimeException.class).hasCause(failure);
        }
    }

    private void assertResponseError(String response, String message) {
        try (var http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(URL)).thenReturn(response);
            assertThatThrownBy(() -> service.getAccessToken("client_credential", "test-app", "test-secret"))
                .isInstanceOf(RuntimeException.class).hasMessage(message);
        }
    }
}
