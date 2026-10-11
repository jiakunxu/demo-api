package com.example.demo.cache.manager;

import com.example.demo.cache.api.RedisService;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisServiceImplTest {

    private static final String KEY = "test:key";
    private static final String VALUE = "test:value";
    private static final Duration DEFAULT_TTL = Duration.ofSeconds(RedisService.DEFAULT_EXP);

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisServiceImpl<String, String> service;

    @Test
    void addUsesDefaultExpiry() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(KEY, VALUE, DEFAULT_TTL)).thenReturn(true);

        assertThat(service.add(KEY, VALUE)).isEqualTo(VALUE);
        verify(valueOperations).setIfAbsent(KEY, VALUE, DEFAULT_TTL);
    }

    @Test
    void addWithNullDateUsesDefaultExpiry() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(KEY, VALUE, DEFAULT_TTL)).thenReturn(true);

        assertThat(service.add(KEY, VALUE, (Date) null)).isEqualTo(VALUE);
        verify(valueOperations).setIfAbsent(KEY, VALUE, DEFAULT_TTL);
    }

    @Test
    void addUsesTimeoutInSeconds() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(KEY, VALUE, Duration.ofSeconds(60))).thenReturn(true);

        assertThat(service.add(KEY, VALUE, 60)).isEqualTo(VALUE);
        verify(valueOperations).setIfAbsent(KEY, VALUE, Duration.ofSeconds(60));
    }

    @Test
    void addConvertsExpiryDateToRemainingSeconds() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq(KEY), eq(VALUE), any(Duration.class))).thenReturn(true);
        Date expiry = new Date(System.currentTimeMillis() + 60_000);
        long before = System.currentTimeMillis();

        assertThat(service.add(KEY, VALUE, expiry)).isEqualTo(VALUE);

        long after = System.currentTimeMillis();
        ArgumentCaptor<Duration> ttl = ArgumentCaptor.forClass(Duration.class);
        verify(valueOperations).setIfAbsent(eq(KEY), eq(VALUE), ttl.capture());
        assertRemainingSeconds(ttl.getValue(), expiry, before, after);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = false)
    void addRejectsUnsuccessfulResult(Boolean result) {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(KEY, VALUE, DEFAULT_TTL)).thenReturn(result);

        assertUnavailable(() -> service.add(KEY, VALUE), "redis add.");
    }

    @Test
    void addConvertsRedisFailure() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(KEY, VALUE, DEFAULT_TTL)).thenThrow(redisFailure());

        assertUnavailable(() -> service.add(KEY, VALUE), "redis add.");
    }

    @Test
    void setUsesDefaultExpiry() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        assertThat(service.set(KEY, VALUE)).isEqualTo(VALUE);
        verify(valueOperations).set(KEY, VALUE, DEFAULT_TTL);
    }

    @Test
    void setWithNullDateUsesDefaultExpiry() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        assertThat(service.set(KEY, VALUE, (Date) null)).isEqualTo(VALUE);
        verify(valueOperations).set(KEY, VALUE, DEFAULT_TTL);
    }

    @Test
    void setUsesTimeoutInSeconds() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        assertThat(service.set(KEY, VALUE, 60)).isEqualTo(VALUE);
        verify(valueOperations).set(KEY, VALUE, Duration.ofSeconds(60));
    }

    @Test
    void setConvertsExpiryDateToRemainingSeconds() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Date expiry = new Date(System.currentTimeMillis() + 60_000);
        long before = System.currentTimeMillis();

        assertThat(service.set(KEY, VALUE, expiry)).isEqualTo(VALUE);

        long after = System.currentTimeMillis();
        ArgumentCaptor<Duration> ttl = ArgumentCaptor.forClass(Duration.class);
        verify(valueOperations).set(eq(KEY), eq(VALUE), ttl.capture());
        assertRemainingSeconds(ttl.getValue(), expiry, before, after);
    }

    @Test
    void setConvertsRedisFailure() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(redisFailure()).when(valueOperations).set(KEY, VALUE, DEFAULT_TTL);

        assertUnavailable(() -> service.set(KEY, VALUE), "redis set.");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = VALUE)
    void getReturnsCachedValueOrNull(String value) {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(KEY)).thenReturn(value);

        assertThat(service.get(KEY)).isEqualTo(value);
        verify(valueOperations).get(KEY);
    }

    @Test
    void getConvertsRedisFailure() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(KEY)).thenThrow(redisFailure());

        assertUnavailable(() -> service.get(KEY), "redis get.");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = { true, false })
    void removeDoesNotRequireAnExistingKey(Boolean result) {
        when(redisTemplate.delete(KEY)).thenReturn(result);

        assertThatCode(() -> service.remove(KEY)).doesNotThrowAnyException();
        verify(redisTemplate).delete(KEY);
    }

    @Test
    void removeConvertsRedisFailure() {
        when(redisTemplate.delete(KEY)).thenThrow(redisFailure());

        assertUnavailable(() -> service.remove(KEY), "redis remove.");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = { true, false })
    void hasKeyIsTrueOnlyForTrueResult(Boolean result) {
        when(redisTemplate.hasKey(KEY)).thenReturn(result);

        assertThat(service.hasKey(KEY)).isEqualTo(Boolean.TRUE.equals(result));
        verify(redisTemplate).hasKey(KEY);
    }

    @Test
    void hasKeyReturnsFalseOnRedisFailure() {
        when(redisTemplate.hasKey(KEY)).thenThrow(redisFailure());

        assertThat(service.hasKey(KEY)).isFalse();
    }

    @Test
    void expireRefreshesDefaultExpiry() {
        when(redisTemplate.expire(KEY, DEFAULT_TTL)).thenReturn(true);

        assertThatCode(() -> service.expire(KEY)).doesNotThrowAnyException();
        verify(redisTemplate).expire(KEY, DEFAULT_TTL);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = false)
    void expireRejectsUnsuccessfulResult(Boolean result) {
        when(redisTemplate.expire(KEY, DEFAULT_TTL)).thenReturn(result);

        assertUnavailable(() -> service.expire(KEY), "redis expire.");
    }

    @Test
    void expireConvertsRedisFailure() {
        when(redisTemplate.expire(KEY, DEFAULT_TTL)).thenThrow(redisFailure());

        assertUnavailable(() -> service.expire(KEY), "redis expire.");
    }

    private static RedisConnectionFailureException redisFailure() {
        return new RedisConnectionFailureException("Redis unavailable in test");
    }

    private static void assertUnavailable(Runnable action, String message) {
        ServiceException exception = catchThrowableOfType(ServiceException.class, action::run);
        assertThat(exception).hasMessage(message);
        assertThat(exception.getCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    private static void assertRemainingSeconds(Duration ttl, Date expiry, long before, long after) {
        // Bound the actual call time instead of sleeping or assuming an exact clock tick.
        assertThat(ttl.getSeconds()).isBetween((expiry.getTime() - after) / 1000,
            (expiry.getTime() - before) / 1000);
        assertThat(ttl.getNano()).isZero();
    }
}
