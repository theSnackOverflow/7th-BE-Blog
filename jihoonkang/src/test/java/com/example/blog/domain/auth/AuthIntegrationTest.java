package com.example.blog.domain.auth;

import com.example.blog.domain.auth.client.KakaoOAuthClient;
import com.example.blog.domain.auth.dto.KakaoTokenResponse;
import com.example.blog.domain.auth.dto.KakaoUserInfo;
import com.example.blog.domain.auth.dto.LoginRequest;
import com.example.blog.domain.auth.dto.SignUpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KakaoOAuthClient kakaoOAuthClient;

    @Value("${kakao.authorize-uri}")
    private String authorizeUri;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Test
    void 회원가입_성공() throws Exception {
        SignUpRequest request = new SignUpRequest("test@example.com", "pass123!", "테스트유저", null);

        mockMvc.perform(post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void 회원가입_이메일_중복_409() throws Exception {
        SignUpRequest request = new SignUpRequest("dup@example.com", "pass123!", "유저1", null);
        mockMvc.perform(post("/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

        SignUpRequest dup = new SignUpRequest("dup@example.com", "pass123!", "유저2", null);
        mockMvc.perform(post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dup)))
            .andExpect(status().isConflict());
    }

    @Test
    void 로그인_성공_AT_반환() throws Exception {
        SignUpRequest signup = new SignUpRequest("login@example.com", "pass123!", "로그인유저", null);
        mockMvc.perform(post("/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signup)));

        LoginRequest login = new LoginRequest("login@example.com", "pass123!");
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.access_token").isNotEmpty())
            .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    void 토큰_없이_보호_API_401() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 유효한_AT로_보호_API_접근_성공() throws Exception {
        SignUpRequest signup = new SignUpRequest("auth@example.com", "pass123!", "인증유저", null);
        mockMvc.perform(post("/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signup)));

        LoginRequest login = new LoginRequest("auth@example.com", "pass123!");
        MvcResult loginResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseBody)
            .path("data").path("access_token").asText();

        mockMvc.perform(get("/api/v1/posts")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk());
    }

    @Test
    void Refresh_Token으로_AT_재발급_성공() throws Exception {
        SignUpRequest signup = new SignUpRequest("reissue@example.com", "pass123!", "재발급유저", null);
        mockMvc.perform(post("/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signup)));

        LoginRequest login = new LoginRequest("reissue@example.com", "pass123!");
        MvcResult loginResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andReturn();

        jakarta.servlet.http.Cookie rtCookie = loginResult.getResponse().getCookie("refreshToken");

        mockMvc.perform(post("/reissue").cookie(rtCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.access_token").isNotEmpty());
    }

    @Test
    void 잘못된_RT로_재발급_401() throws Exception {
        jakarta.servlet.http.Cookie fakeCookie =
            new jakarta.servlet.http.Cookie("refreshToken", "invalid.rt.token");

        mockMvc.perform(post("/reissue").cookie(fakeCookie))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 카카오_로그인_시작_카카오_인가_URL로_리다이렉트() throws Exception {
        mockMvc.perform(get("/oauth/kakao/login"))
            .andExpect(status().is3xxRedirection())
            .andExpect(result -> {
                String location = result.getResponse().getHeader("Location");
                assertThat(location).startsWith(authorizeUri);
                assertThat(location).contains("client_id=" + clientId);
                assertThat(location).contains("response_type=code");
            });
    }

    @Test
    void 카카오_콜백_신규유저_액세스_토큰_반환() throws Exception {
        KakaoTokenResponse kakaoToken = new KakaoTokenResponse("kakao_at", "kakao_rt", "bearer", 21599L);
        KakaoUserInfo userInfo = new KakaoUserInfo(
            55555L,
            new KakaoUserInfo.KakaoAccount("integration@kakao.com",
                new KakaoUserInfo.KakaoAccount.Profile("통합테스트닉", null))
        );

        given(kakaoOAuthClient.getToken(anyString())).willReturn(kakaoToken);
        given(kakaoOAuthClient.getUserInfo(anyString())).willReturn(userInfo);

        mockMvc.perform(get("/oauth/kakao/callback").param("code", "testcode"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.data.access_token").isNotEmpty())
            .andExpect(cookie().exists("refreshToken"));
    }
}
