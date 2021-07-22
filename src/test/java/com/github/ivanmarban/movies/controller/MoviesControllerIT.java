package com.github.ivanmarban.movies.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ivanmarban.movies.model.Movie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static java.lang.String.format;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = MoviesControllerIT.Initializer.class)
@DisplayName("MoviesController Integration Tests")
@AutoConfigureMockMvc
public class MoviesControllerIT {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

    @Autowired
    private MoviesController moviesController;

    @AfterEach
    private void cleanDatabase() {
        moviesController.deleteAllMovies();
    }

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Create movie")
    public void createMovie() throws Exception {
        Movie movie = Movie.builder().title("2001: A Space Odyssey").year("1968").rated("G").runtime("160 min").genre("Mystery, Sci-Fi")
            .director("Stanley Kubrick").build();

        mvc.perform(MockMvcRequestBuilders
            .post("/api/movies")
            .content(asJsonString(movie))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").exists());
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                format("spring.data.mongodb.uri=mongodb://%s:%s/movies", mongoDBContainer.getContainerIpAddress(),
                    mongoDBContainer.getMappedPort(27017)));
        }
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
