package com.example.demo.framework.util;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author JiakunXu
 */
public class HttpUtil {

    private static final int STATUS_CODE_200 = 200;

    private static final int STATUS_CODE_300 = 300;

    /**
     * @param uri
     * @return
     * @throws Exception
     */
    public static String get(String uri) throws Exception {
        return get(uri, null);
    }

    /**
     *
     * @param uri
     * @param header
     * @return
     * @throws Exception
     */
    public static String get(String uri, Map<String, String> header) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ClassicHttpRequest request = ClassicRequestBuilder.get(uri)
                .setHeaders(getHeaders(header)).build();

            return client.execute(request, response -> {
                int status = response.getCode();
                final HttpEntity entity = response.getEntity();
                try {
                    if (status >= STATUS_CODE_200 && status < STATUS_CODE_300) {
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                } finally {
                    EntityUtils.consume(entity);
                }
            });
        }
    }

    /**
     * @param uri
     * @param parameter
     * @return
     * @throws Exception
     */
    public static String post(String uri, Map<String, String> parameter) throws Exception {
        return post(uri, parameter, null);
    }

    /**
     * @param uri
     * @param parameter
     * @param header
     * @return
     * @throws Exception
     */
    public static String post(String uri, Map<String, String> parameter,
                              Map<String, String> header) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ClassicHttpRequest request = ClassicRequestBuilder.post(uri)
                .setHeaders(getHeaders(header))
                .setEntity(
                    new UrlEncodedFormEntity(getParameters(parameter), StandardCharsets.UTF_8))
                .build();

            return client.execute(request, response -> {
                int status = response.getCode();
                final HttpEntity entity = response.getEntity();
                try {
                    if (status >= STATUS_CODE_200 && status < STATUS_CODE_300) {
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                } finally {
                    EntityUtils.consume(entity);
                }
            });
        }
    }

    /**
     * @param uri
     * @param parameter
     * @return
     * @throws Exception
     */
    public static String post(String uri, String parameter) throws Exception {
        return post(uri, parameter, null);
    }

    /**
     * @param uri
     * @param parameter
     * @return
     * @throws Exception
     */
    public static String post(String uri, String parameter,
                              Map<String, String> header) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ClassicHttpRequest request = ClassicRequestBuilder.post(uri)
                .setHeaders(getHeaders(header))
                .setEntity(new StringEntity(parameter, ContentType.APPLICATION_JSON)).build();

            return client.execute(request, response -> {
                int status = response.getCode();
                final HttpEntity entity = response.getEntity();
                try {
                    if (status >= STATUS_CODE_200 && status < STATUS_CODE_300) {
                        return entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8)
                            : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                } finally {
                    EntityUtils.consume(entity);
                }
            });
        }
    }

    /**
     * @param uri
     * @param parameter0
     * @param parameter1
     * @return
     * @throws Exception
     */
    public static String post(String uri, Map<String, String> parameter0,
                              Map<String, InputStream> parameter1,
                              Map<String, String> header) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

            for (Map.Entry<String, String> map : parameter0.entrySet()) {
                multipartEntityBuilder.addTextBody(map.getKey(), map.getValue(),
                    ContentType.APPLICATION_FORM_URLENCODED.withCharset(StandardCharsets.UTF_8));
            }

            for (Map.Entry<String, InputStream> map : parameter1.entrySet()) {
                multipartEntityBuilder.addBinaryBody(map.getKey(), map.getValue(),
                    ContentType.MULTIPART_FORM_DATA, "");
            }

            ClassicHttpRequest request = ClassicRequestBuilder.post(uri)
                .setHeaders(getHeaders(header)).setEntity(multipartEntityBuilder.build()).build();

            return client.execute(request, response -> {
                int status = response.getCode();
                final HttpEntity entity = response.getEntity();
                try {
                    if (status >= STATUS_CODE_200 && status < STATUS_CODE_300) {
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                } finally {
                    EntityUtils.consume(entity);
                }
            });
        }
    }

    public static String put(String uri, InputStream content, String contentType) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ClassicHttpRequest request = ClassicRequestBuilder.put(uri)
                .setEntity(new InputStreamEntity(content, ContentType.parse(contentType))).build();

            return client.execute(request, response -> {
                int status = response.getCode();
                final HttpEntity entity = response.getEntity();
                try {
                    if (status >= STATUS_CODE_200 && status < STATUS_CODE_300) {
                        return entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8)
                            : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                } finally {
                    EntityUtils.consume(entity);
                }
            });
        }
    }

    /**
     * @param uri
     * @return
     * @throws Exception
     */
    public static byte[] download(String uri) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ClassicHttpRequest request = ClassicRequestBuilder.get(uri).build();

            return client.execute(request, response -> {
                int status = response.getCode();
                final HttpEntity entity = response.getEntity();
                try {
                    if (status >= STATUS_CODE_200 && status < STATUS_CODE_300) {
                        return entity != null ? EntityUtils.toByteArray(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                } finally {
                    EntityUtils.consume(entity);
                }
            });
        }
    }

    public static byte[] download(String uri, String parameter) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ClassicHttpRequest request = ClassicRequestBuilder.post(uri)
                .setEntity(new StringEntity(parameter, ContentType.APPLICATION_JSON)).build();

            return client.execute(request, response -> {
                int status = response.getCode();
                final HttpEntity entity = response.getEntity();
                try {
                    if (status >= STATUS_CODE_200 && status < STATUS_CODE_300) {
                        return entity != null ? EntityUtils.toByteArray(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                } finally {
                    EntityUtils.consume(entity);
                }
            });
        }
    }

    /**
     * 将传入的键/值对参数转换为NameValuePair参数集.
     *
     * @param parameter 参数集, 键/值对
     * @return NameValuePair参数集
     */
    private static List<NameValuePair> getParameters(Map<String, String> parameter) {
        if (parameter == null || parameter.isEmpty()) {
            return null;
        }

        List<NameValuePair> parameters = new ArrayList<>();

        for (Map.Entry<String, String> map : parameter.entrySet()) {
            parameters.add(new BasicNameValuePair(map.getKey(), map.getValue()));
        }

        return parameters;
    }

    private static Header[] getHeaders(Map<String, String> header) {
        if (header == null || header.isEmpty()) {
            return null;
        }

        Header[] headers = new Header[header.size()];
        int i = 0;

        for (Map.Entry<String, String> map : header.entrySet()) {
            headers[i++] = new BasicHeader(map.getKey(), map.getValue());
        }

        return headers;
    }

}
