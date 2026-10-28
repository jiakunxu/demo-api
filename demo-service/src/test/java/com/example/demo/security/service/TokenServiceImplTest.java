package com.example.demo.security.service;

import com.example.demo.cache.api.RedisService;
import com.example.demo.security.api.RefreshTokenService;
import com.example.demo.security.api.UserDetailsService;
import com.example.demo.security.api.bo.LoginUser;
import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigInteger;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenServiceImplTest {
    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
    private final RedisService<String, UserDetails> redis = mock(RedisService.class);
    private final RefreshTokenService refresh = mock(RefreshTokenService.class);
    private final UserDetailsService users = mock(UserDetailsService.class);
    private final TokenServiceImpl service = new TokenServiceImpl();
    private final LoginUser user = new LoginUser();

    @BeforeEach
    void initialize() {
        ReflectionTestUtils.setField(service, "redisService", redis);
        ReflectionTestUtils.setField(service, "refreshTokenService", refresh);
        ReflectionTestUtils.setField(service, "userDetailsService", users);
        ReflectionTestUtils.setField(service, "secretKey", SECRET);
        user.setId(BigInteger.TEN);
    }

    private String token(String id) {
        return Jwts.builder().id(id).signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS512).compact();
    }

    @Test
    void issuedJwtIdentifiesTheCachedUserAndUsesHs512() {
        String jwt = service.setUser(user);
        var parsed = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).build().parseSignedClaims(jwt);
        assertThat(parsed.getHeader().getAlgorithm()).isEqualTo("HS512");
        assertThat(parsed.getPayload().getId()).matches("[0-9a-f-]{36}");
        verify(redis).add(parsed.getPayload().getId(), user);
        verify(refresh).set(user);
    }

    @Test
    void cachedValidUserAvoidsReload() {
        when(redis.get("id")).thenReturn(user);
        when(refresh.validate(user)).thenReturn(true);
        assertThat(service.getUser(token("id"))).isSameAs(user);
        verifyNoInteractions(users);
        verify(redis, never()).set(anyString(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Bearer "})
    void staleRevisionReloadsAndRefreshesUser(String prefix) {
        LoginUser fresh = new LoginUser();
        fresh.setId(BigInteger.TEN);
        when(redis.get("id")).thenReturn(user);
        when(users.loadUserById(BigInteger.TEN)).thenReturn(fresh);
        assertThat(service.getUser(prefix + token("id"))).isSameAs(fresh);
        verify(redis).get("id");
        verify(redis).set("id", fresh);
        verifyNoMoreInteractions(redis);
        verify(refresh).set(fresh);
    }

    @ParameterizedTest
    @ValueSource(strings = {"locked", "disabled", "expired", "missing", "reload error"})
    void staleUserThatCannotAuthenticateIsRemoved(String reason) {
        when(redis.get("id")).thenReturn(user);
        LoginUser fresh = new LoginUser();
        if (reason.equals("locked")) fresh.setAccountNonLocked(false);
        if (reason.equals("disabled")) fresh.setEnabled(false);
        if (reason.equals("expired")) fresh.setAccountNonExpired(false);
        if (reason.equals("reload error")) when(users.loadUserById(BigInteger.TEN)).thenThrow(new RuntimeException("reload failed"));
        else when(users.loadUserById(BigInteger.TEN)).thenReturn(reason.equals("missing") ? null : fresh);
        assertThat(service.getUser(token("id"))).isNull();
        verify(redis).remove("id");
        verify(redis, never()).set(anyString(), any());
        verify(refresh, never()).set(any());
    }

    @Test
    void missingCachedUserHasNoRefreshWork() {
        assertThat(service.getUser(token("id"))).isNull();
        verifyNoInteractions(refresh, users);
    }

    @Test
    void refreshAndRemoveExtractJwtIdIncludingBearerPrefix() {
        service.refresh("Bearer " + token("id"));
        service.remove(token("id"));
        verify(redis).expire("id");
        verify(redis).remove("id");
    }

    @Test
    void blankTokensAreNoOpsAndInvalidSignatureCannotTouchRedis() {
        assertThat(service.getUser(" ")).isNull();
        service.refresh(null);
        service.remove("");
        assertThatThrownBy(() -> service.getUser("invalid.jwt")).isInstanceOf(io.jsonwebtoken.JwtException.class);
        String foreign = Jwts.builder().id("id").signWith(Keys.hmacShaKeyFor((SECRET + "different").getBytes(StandardCharsets.UTF_8))).compact();
        assertThatThrownBy(() -> service.getUser(foreign)).isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
        verifyNoInteractions(redis);
    }

    @Test
    void staleForeignUserDetailsAreNotReloadedAsApplicationUsers() {
        UserDetails foreign = mock(UserDetails.class);
        when(redis.get("id")).thenReturn(foreign);
        assertThat(service.getUser(token("id"))).isNull();
        verifyNoInteractions(users);
    }
}
