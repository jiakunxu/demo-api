package com.example.demo.framework.util;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpUtilTest {
    private static final String URI = "https://unit.invalid/resource?x=1";
    private static final String BODY = "响应内容";
    private static final Map<String, String> HEADERS = Map.of("X-Test", "token");

    enum RequestCase {
        GET, GET_HEADERS, FORM, FORM_HEADERS, JSON, JSON_HEADERS, MULTIPART, PUT, DOWNLOAD, DOWNLOAD_POST;

        Object invoke() throws Exception {
            return switch (this) {
                case GET -> HttpUtil.get(URI);
                case GET_HEADERS -> HttpUtil.get(URI, HEADERS);
                case FORM -> HttpUtil.post(URI, Map.of("name", "张 三", "symbol", "a&b"));
                case FORM_HEADERS -> HttpUtil.post(URI, Map.of("name", "张 三", "symbol", "a&b"), HEADERS);
                case JSON -> HttpUtil.post(URI, "{\"name\":\"张三\"}");
                case JSON_HEADERS -> HttpUtil.post(URI, "{\"name\":\"张三\"}", HEADERS);
                case MULTIPART -> HttpUtil.post(URI, Map.of("name", "张三"),
                    Map.of("file", new ByteArrayInputStream("file-content".getBytes(StandardCharsets.UTF_8))), HEADERS);
                case PUT -> HttpUtil.put(URI, new ByteArrayInputStream("file-content".getBytes(StandardCharsets.UTF_8)), "text/plain");
                case DOWNLOAD -> HttpUtil.download(URI);
                case DOWNLOAD_POST -> HttpUtil.download(URI, "{\"name\":\"张三\"}");
            };
        }

        void verifyRequest(ClassicHttpRequest request) throws Exception {
            assertThat(request.getUri().toString()).isEqualTo(URI);
            assertThat(request.getMethod()).isEqualTo(switch (this) {
                case GET, GET_HEADERS, DOWNLOAD -> "GET";
                case PUT -> "PUT";
                default -> "POST";
            });
            if (this == GET_HEADERS || this == FORM_HEADERS || this == JSON_HEADERS || this == MULTIPART) {
                assertThat(request.getFirstHeader("X-Test")).isNotNull();
                assertThat(request.getFirstHeader("X-Test").getValue()).isEqualTo("token");
            }
            switch (this) {
                case FORM, FORM_HEADERS -> {
                    assertThat(request.getEntity().getContentType()).startsWith("application/x-www-form-urlencoded");
                    String body = EntityUtils.toString(request.getEntity(), StandardCharsets.UTF_8);
                    assertThat(body.split("&")).containsExactlyInAnyOrder("name=%E5%BC%A0+%E4%B8%89", "symbol=a%26b");
                }
                case JSON, JSON_HEADERS, DOWNLOAD_POST -> {
                    assertThat(request.getEntity().getContentType()).startsWith("application/json");
                    assertThat(EntityUtils.toString(request.getEntity(), StandardCharsets.UTF_8)).isEqualTo("{\"name\":\"张三\"}");
                }
                case MULTIPART -> {
                    assertThat(request.getEntity().getContentType()).startsWith("multipart/form-data;").contains("boundary=");
                    var out = new java.io.ByteArrayOutputStream();
                    request.getEntity().writeTo(out);
                    assertThat(out.toString(StandardCharsets.UTF_8)).contains("name=\"name\"", "张三", "name=\"file\"", "file-content");
                }
                case PUT -> {
                    assertThat(request.getFirstHeader(HttpHeaders.CONTENT_TYPE)).isNotNull();
                    assertThat(request.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue())
                        .isEqualTo("text/plain");
                    assertThat(request.getEntity().getContentType()).startsWith("text/plain");
                    assertThat(EntityUtils.toString(request.getEntity())).isEqualTo("file-content");
                }
                default -> assertThat(request.getEntity()).isNull();
            }
        }
    }

    static Stream<Object[]> statusCases() {
        return Arrays.stream(RequestCase.values()).flatMap(request ->
            Stream.of(199, 200, 201, 206, 299, 300, 400, 500).map(status -> new Object[] {request, status}));
    }

    @ParameterizedTest(name = "{0} / status {1}")
    @MethodSource("statusCases")
    void constructsRequestChecksStatusAndClosesResources(RequestCase requestCase, int status) throws Exception {
        CloseableHttpClient client = mock(CloseableHttpClient.class);
        ClosingStream content = new ClosingStream(BODY.getBytes(StandardCharsets.UTF_8));
        when(client.execute(any(ClassicHttpRequest.class), org.mockito.ArgumentMatchers.<HttpClientResponseHandler<Object>>any()))
            .thenAnswer(invocation -> {
                requestCase.verifyRequest(invocation.getArgument(0));
                HttpClientResponseHandler<Object> handler = invocation.getArgument(1);
                var response = new BasicClassicHttpResponse(status);
                response.setEntity(new InputStreamEntity(content, ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8)));
                return handler.handleResponse(response);
            });
        try (var factory = mockStatic(HttpClients.class)) {
            factory.when(HttpClients::createDefault).thenReturn(client);
            if (status >= 200 && status < 300) {
                Object result = requestCase.invoke();
                if (result instanceof byte[] bytes) assertThat(bytes).containsExactly(BODY.getBytes(StandardCharsets.UTF_8));
                else assertThat(result).isEqualTo(BODY);
            } else {
                assertThatThrownBy(requestCase::invoke).isInstanceOf(ClientProtocolException.class)
                    .hasMessage("Unexpected response status: " + status);
            }
            assertThat(content.closed).isTrue();
            verify(client).close();
        }
    }

    @ParameterizedTest
    @EnumSource(RequestCase.class)
    void successfulResponseWithoutEntityReturnsNull(RequestCase requestCase) throws Exception {
        CloseableHttpClient client = mock(CloseableHttpClient.class);
        when(client.execute(any(ClassicHttpRequest.class), org.mockito.ArgumentMatchers.<HttpClientResponseHandler<Object>>any()))
            .thenAnswer(invocation -> {
                HttpClientResponseHandler<Object> handler = invocation.getArgument(1);
                return handler.handleResponse(new BasicClassicHttpResponse(204));
            });
        try (var factory = mockStatic(HttpClients.class)) {
            factory.when(HttpClients::createDefault).thenReturn(client);
            assertThat(requestCase.invoke()).isNull();
            verify(client).close();
        }
    }

    @ParameterizedTest
    @EnumSource(RequestCase.class)
    void transportFailurePropagatesAndClosesClient(RequestCase requestCase) throws Exception {
        CloseableHttpClient client = mock(CloseableHttpClient.class);
        IOException failure = new IOException("connection failed");
        when(client.execute(any(ClassicHttpRequest.class), org.mockito.ArgumentMatchers.<HttpClientResponseHandler<Object>>any())).thenThrow(failure);
        try (var factory = mockStatic(HttpClients.class)) {
            factory.when(HttpClients::createDefault).thenReturn(client);
            assertThatThrownBy(requestCase::invoke).isSameAs(failure);
            verify(client).close();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "text/plain; charset=UTF-8",
        "application/json; charset=UTF-8",
        "application/octet-stream; version=1"
    })
    void putPreservesContentTypeParametersAndPayload(String contentType) throws Exception {
        CloseableHttpClient client = mock(CloseableHttpClient.class);
        byte[] payload = BODY.getBytes(StandardCharsets.UTF_8);
        when(client.execute(any(ClassicHttpRequest.class),
            org.mockito.ArgumentMatchers.<HttpClientResponseHandler<Object>>any()))
            .thenAnswer(invocation -> {
                ClassicHttpRequest request = invocation.getArgument(0);
                assertThat(request.getMethod()).isEqualTo("PUT");
                assertThat(request.getUri().toString()).isEqualTo(URI);
                assertThat(request.getHeaders(HttpHeaders.CONTENT_TYPE)).hasSize(1);
                assertThat(request.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue())
                    .isEqualTo(contentType);
                assertThat(request.getEntity().getContentType()).isEqualTo(contentType);
                assertThat(EntityUtils.toByteArray(request.getEntity())).containsExactly(payload);
                HttpClientResponseHandler<Object> handler = invocation.getArgument(1);
                return handler.handleResponse(new BasicClassicHttpResponse(204));
            });

        try (var factory = mockStatic(HttpClients.class);
             var content = new ByteArrayInputStream(payload)) {
            factory.when(HttpClients::createDefault).thenReturn(client);

            assertThat(HttpUtil.put(URI, content, contentType)).isNull();

            verify(client).close();
        }
    }

    private static class ClosingStream extends ByteArrayInputStream {
        private boolean closed;
        ClosingStream(byte[] bytes) { super(bytes); }
        public void close() throws IOException { closed = true; super.close(); }
    }
}
