package com.boss.bossuserservice.controller;

import com.boss.bosscommon.pojo.dto.UserJobApplyDTO;
import com.boss.bosscommon.pojo.dto.UserLoginPasswordDTO;
import com.boss.bosscommon.pojo.dto.UserLogoutDTO;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class CandidateControllerTest {

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
        assertNotNull(token, "Token should not be null");
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockMvc.perform(post("/user/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .content(objectMapper.writeValueAsString(
                                UserLogoutDTO.builder()
                                        .phone(TEST_PHONE)
                                        .role(TEST_ROLE)
                                        .build()
                        )))
                .andExpect(status().isOk());
    }

    @Test
    public void testCandidateApplyForJob() throws Exception {
        UserJobApplyDTO userJobApplyDTO = UserJobApplyDTO.builder()
                .jobUid(1L)
                .applyMsg("我对这个职位很感兴趣")
                .build();

        mockMvc.perform(post("/candidate/jobs")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userJobApplyDTO)))
                .andExpect(status().isOk());
    }
}