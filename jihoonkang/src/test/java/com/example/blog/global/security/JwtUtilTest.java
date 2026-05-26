package com.example.blog.global.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "LeetsBackendKx5!93Jv#1Rz@lQwT9pXe3bD7sUaFzYt");
        ReflectionTestUtils.setField(jwtUtil, "accessExpiration", 1800000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 1209600000L);
        jwtUtil.init();
    }

    @Test
    void generateAccessToken_정상_생성() {
        String token = jwtUtil.generateAccessToken(1L, "USER");

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.validate(token)).isTrue();
        assertThat(jwtUtil.getUserId(token)).isEqualTo(1L);
        assertThat(jwtUtil.getRole(token)).isEqualTo("USER");
    }

    @Test
    void generateRefreshToken_정상_생성() {
        String token = jwtUtil.generateRefreshToken(1L);

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.validate(token)).isTrue();
        assertThat(jwtUtil.getUserId(token)).isEqualTo(1L);
    }

    @Test
    void validate_만료된_토큰_false_반환() {
        JwtUtil expiredUtil = new JwtUtil();
        ReflectionTestUtils.setField(expiredUtil, "secret", "LeetsBackendKx5!93Jv#1Rz@lQwT9pXe3bD7sUaFzYt");
        ReflectionTestUtils.setField(expiredUtil, "accessExpiration", -1000L);
        ReflectionTestUtils.setField(expiredUtil, "refreshExpiration", -1000L);
        expiredUtil.init();

        String token = expiredUtil.generateAccessToken(1L, "USER");

        assertThat(expiredUtil.validate(token)).isFalse();
    }

    @Test
    void validate_위조된_토큰_false_반환() {
        assertThat(jwtUtil.validate("invalid.token.value")).isFalse();
    }

    @Test
    void validate_서명이_다른_토큰_false_반환() {
        JwtUtil otherUtil = new JwtUtil();
        ReflectionTestUtils.setField(otherUtil, "secret", "AnotherSecretKeyThatIsLongEnoughForHS256AlgorithmUse");
        ReflectionTestUtils.setField(otherUtil, "accessExpiration", 1800000L);
        ReflectionTestUtils.setField(otherUtil, "refreshExpiration", 1209600000L);
        otherUtil.init();

        String token = otherUtil.generateAccessToken(1L, "USER");

        assertThat(jwtUtil.validate(token)).isFalse();
    }
}
