package com.boss.bossuserservice.controller;

import com.boss.bosscommon.pojo.dto.UserLoginPasswordDTO;
import com.boss.bosscommon.pojo.dto.UserLogoutDTO;
import com.boss.bosscommon.pojo.dto.UserRegistryDTO;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class AuthControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterAndLogout() throws Exception {

        UserRegistryDTO userRegistryDTO = UserRegistryDTO.builder()
                .phone("187" + (int)(Math.random() * 1e8))
                .role((int)(Math.random() * 10 % 2) == 0 ? 0 : 1)
                .password("123456")
                .build();

        String registerObject = mockMvc
                .perform(post("/user/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistryDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        log.info("用户信息：{}", registerObject);

        String loginObject = mockMvc
                .perform(post("/user/auth/login/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                UserLoginPasswordDTO.builder()
                                        .phone(userRegistryDTO.getPhone())
                                        .password(userRegistryDTO.getPassword())
                                        .role(userRegistryDTO.getRole())
                                        .build())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        log.info("用户信息：{}", loginObject);

        String token = objectMapper.readValue(loginObject, UserBasicVO.class).getAuthorization();

        String logoutObject = mockMvc
                .perform(post("/user/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .content(objectMapper.writeValueAsString(
                                UserLogoutDTO.builder()
                                        .phone(userRegistryDTO.getPhone())
                                        .role(userRegistryDTO.getRole())
                                        .build()
                        )))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        log.info("登出信息：{}", logoutObject);
    }

}