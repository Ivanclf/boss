package com.boss.bossuserservice.controller;

import com.boss.bosscommon.pojo.dto.UserLoginPasswordDTO;
import com.boss.bosscommon.pojo.dto.UserLogoutDTO;
import com.boss.bosscommon.pojo.dto.UserUpdateDTO;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class ProfileControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private ObjectMapper objectMapper;

    private String token;
    private static final String TEST_PHONE = "18712345678";
    private static final Integer TEST_ROLE = 0;
    private static final String TEST_PASSWORD = "123456";

    @BeforeEach
    public void setUp() throws Exception {
        String loginObject = mockMvc
                .perform(post("/user/auth/login/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                UserLoginPasswordDTO.builder()
                                        .phone(TEST_PHONE)
                                        .password(TEST_PASSWORD)
                                        .role(TEST_ROLE)
                                        .build())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserBasicVO userBasicVO = objectMapper.readValue(loginObject, UserBasicVO.class);
        token = userBasicVO.getAuthorization();
        assertNotNull(token, "Token 不应该为空");
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockMvc
                .perform(post("/user/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .content(objectMapper.writeValueAsString(
                                UserLogoutDTO.builder()
                                        .phone(TEST_PHONE)
                                        .role(TEST_ROLE)
                                        .build())))
                .andExpect(status().isOk());
        log.info("已成功登出");
    }

    @Test
    public void testUserProfileOperations() throws Exception {

        MvcResult getResult = mockMvc
                .perform(get("/user/profile")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn();

        String userProfile = getResult.getResponse().getContentAsString();
        log.info("用户信息：{}", userProfile);

        UserUpdateDTO userUpdateDTO = UserUpdateDTO.builder()
                .phone(TEST_PHONE)
                .role(TEST_ROLE)
                .password("123456")
                .build();

        mockMvc
                .perform(put("/user/profile")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void testElasticsearchEndpoints() throws Exception {
        MvcResult esApplyResult = mockMvc
                .perform(get("/user/profile/es/apply"))
                .andExpect(status().isOk())
                .andReturn();

        String esApplyData = esApplyResult.getResponse().getContentAsString();
        log.info("ES申请信息：{}", esApplyData);

        MvcResult esUserResult = mockMvc
                .perform(get("/user/profile/es/user/1"))
                .andExpect(status().isOk())
                .andReturn();

        String esUserData = esUserResult.getResponse().getContentAsString();
        log.info("ES用户信息：{}", esUserData);

        MvcResult esApplyUserResult = mockMvc
                .perform(get("/user/profile/es/apply/1"))
                .andExpect(status().isOk())
                .andReturn();

        String esApplyUserData = esApplyUserResult.getResponse().getContentAsString();
        log.info("ES用户申请信息：{}", esApplyUserData);
    }
}