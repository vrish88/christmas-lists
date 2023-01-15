package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.attoparser.dom.INode;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void validatePersonCreation() throws Exception {
        mockMvc.perform(post("/person"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void personCanBeCreated() throws Exception {
		String location = mockMvc.perform(post("/person")
				.with(csrf())
				.param("name", "Bobo")
				.param("buyingFor", "Stevie"))
			.andExpect(status().isNoContent())
			.andReturn()
			.getResponse()
			.getHeader("HX-Location");

		mockMvc.perform(get(location))
			.andExpect(content().string(Matchers.containsString("Bobo")))
			.andExpect(content().string(Matchers.containsString("Stevie")));
    }

    private String toJson(Map<String, String> map) {
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
