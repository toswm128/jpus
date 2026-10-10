package com.minsu.jpus.user;

import com.minsu.jpus.user.dto.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(
    classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD
)
public class UserControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  UserService userService;



  @Test
  void createUserTest() throws Exception  {
    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "username":"minsu",
              "nickname":"조조민수"
            }
            """)
    ).andExpect(status().isCreated()).andExpect(jsonPath("$.username").value("minsu"))
        .andExpect(jsonPath("$.nickname").value("조조민수"));
  }


  @Test
  void getUsersTest() throws Exception {
    userService.createUser(new CreateUserRequest("minsu","test1234","조조민수"));

    mockMvc.perform(
            get("/users")
        ).andExpect(status().isOk())
        .andExpect(content().json("""
            [{
              "nickname": "조조민수",
              "id": 1,
              "username": "minsu"
            }]
            """));
  }


  @Test
  void getUserTest() throws Exception{
    userService.createUser(new CreateUserRequest("minsu","test1234","조조민수"));

    mockMvc.perform(
            get("/users/1")
        ).andExpect(status().isOk())
        .andExpect(content().json("""
            {
              "nickname": "조조민수",
              "id": 1,
              "username": "minsu"
            }
            """));
  }

  @Test
  void getUserNotFoundTest() throws Exception{
    mockMvc.perform(
            get("/users/123")
        ).andExpect(status().isNotFound())
        .andExpect(content().string("존재하지 않는 유저입니다. id=123"));
  }
}
