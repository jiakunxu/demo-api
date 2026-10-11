package com.example.demo.captcha.manager;

import com.example.demo.cache.api.RedisService;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaptchaServiceImplTest {

    private static final String UUID = "captcha-id";
    private static final String KEY = RedisService.CACHE_KEY_CAPTCHA + UUID;

    @Mock
    private RedisService<String, String> redisService;
    @InjectMocks
    private CaptchaServiceImpl service;

    @Test
    void generatesImageAndCachesAnswerWithMatchingIdAndExpiry() throws Exception {
        var captcha = service.getCaptcha();

        assertThat(captcha.getCaptchaEnabled()).isTrue();
        assertThat(captcha.getUuid()).isNotBlank();
        ArgumentCaptor<String> answer = ArgumentCaptor.forClass(String.class);
        verify(redisService).add(eq(RedisService.CACHE_KEY_CAPTCHA + captcha.getUuid()),
            answer.capture(), eq(RedisService.CACHE_KEY_CAPTCHA_DEFAULT_EXP));
        assertThat(answer.getValue()).matches("\\d+");
        try (var input = new ByteArrayInputStream(Base64.getDecoder().decode(captcha.getImg()))) {
            var image = ImageIO.read(input);
            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(160);
            assertThat(image.getHeight()).isEqualTo(60);
        }
    }

    @Test
    void generationReportsCacheFailure() {
        when(redisService.add(anyString(), anyString(), eq(RedisService.CACHE_KEY_CAPTCHA_DEFAULT_EXP)))
            .thenThrow(new ServiceException(HttpStatus.SERVICE_UNAVAILABLE, "unavailable"));

        assertValidationError(() -> service.getCaptcha(), "系统正忙，请稍后再试");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void rejectsBlankIdWithoutAccessingRedis(String id) {
        assertValidationError(() -> service.validate(id, "12"), "验证码不能为空");
        verifyNoInteractions(redisService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void rejectsBlankCodeWithoutAccessingRedis(String code) {
        assertValidationError(() -> service.validate(UUID, code), "验证码不能为空");
        verifyNoInteractions(redisService);
    }

    @Test
    void acceptsCorrectCodeAndDeletesTheSameKey() {
        when(redisService.get(KEY)).thenReturn("12");

        assertThatCode(() -> service.validate(UUID, "12")).doesNotThrowAnyException();

        var order = inOrder(redisService);
        order.verify(redisService).get(KEY);
        order.verify(redisService).remove(KEY);
        verifyNoMoreInteractions(redisService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void rejectsExpiredCode(String cachedCode) {
        when(redisService.get(KEY)).thenReturn(cachedCode);

        assertValidationError(() -> service.validate(UUID, "12"), "验证码已失效");
        verify(redisService).remove(KEY);
    }

    @Test
    void rejectsWrongCodeAndConsumesCaptcha() {
        when(redisService.get(KEY)).thenReturn("12");

        assertValidationError(() -> service.validate(UUID, "13"), "验证码错误");
        verify(redisService).remove(KEY);
    }

    @Test
    void propagatesReadFailure() {
        ServiceException failure = new ServiceException(HttpStatus.SERVICE_UNAVAILABLE, "unavailable");
        when(redisService.get(KEY)).thenThrow(failure);

        assertThatThrownBy(() -> service.validate(UUID, "12")).isSameAs(failure);
        verify(redisService, never()).remove(anyString());
    }

    private static void assertValidationError(Runnable action, String message) {
        ServiceException error = catchThrowableOfType(ServiceException.class, action::run);
        assertThat(error).hasMessage(message);
        assertThat(error.getCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
