package com.minsu.jpus.basic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class GreetingServiceTest {

  @Autowired
  MockMvc mockMvc;



  @Test
  void GreetTest() throws Exception {
    mockMvc.perform(
        get("/greeting/hello").
        param("name","minsu")
    ).andExpect(status().isOk())
    .andExpect(content().string("Hello!! minsu"));
  }

  @Test
  void GreetPostTest() throws Exception{
    mockMvc.perform(
        post("/greeting/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
            {
              "name": "minsu",
              "age": 23
            }
            """)

    ).andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("minsu"))
        .andExpect(jsonPath("$.age").value(23));

  }
}
