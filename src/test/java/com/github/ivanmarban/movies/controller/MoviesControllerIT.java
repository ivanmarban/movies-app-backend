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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    @DisplayName("Get all movies")
    public void getMovies() {
        Movie movie1 = Movie.builder().title("2001: A Space Odyssey").year("1968").rated("G").runtime("160 min").genre("Mystery, Sci-Fi")
            .director("Stanley Kubrick").build();
        Movie movie2 = Movie.builder().title("The Shining").year("1980").rated("R").runtime("142 min").genre("Horror, Mystery, Thriller")
            .director("Stanley Kubrick").build();
        moviesController.createMovie(movie1);
        moviesController.createMovie(movie2);
        ResponseEntity<List<Movie>> movies = moviesController.getMovies(null);
        assertNotNull(movies);
        assertTrue(movies.getBody().size() > 0);
        assertEquals(2, movies.getBody().size());
    }

    @Test
    @DisplayName("Get movie by title containing 'Space'")
    public void getMovieByTitle() {
        Movie movie1 = Movie.builder().title("2001: A Space Odyssey").year("1968").rated("G").runtime("160 min").genre("Mystery, Sci-Fi")
            .director("Stanley Kubrick").build();
        Movie movie2 = Movie.builder().title("The Shining").year("1980").rated("R").runtime("142 min").genre("Horror, Mystery, Thriller")
            .director("Stanley Kubrick").build();
        moviesController.createMovie(movie1);
        moviesController.createMovie(movie2);
        ResponseEntity<List<Movie>> movies = moviesController.getMovies("Space");
        assertNotNull(movies);
        assertTrue(movies.getBody().size() > 0);
        assertEquals(1, movies.getBody().size());
        assertEquals("2001: A Space Odyssey", movies.getBody().get(0).getTitle());
    }

    @Test
    @DisplayName("Get movie by ID")
    public void getMovieById() {
        Movie movie1 = Movie.builder().title("2001: A Space Odyssey").year("1968").rated("G").runtime("160 min").genre("Mystery, Sci-Fi")
            .director("Stanley Kubrick").build();
        Movie movie2 = Movie.builder().title("The Shining").year("1980").rated("R").runtime("142 min").genre("Horror, Mystery, Thriller")
            .director("Stanley Kubrick").build();
        moviesController.createMovie(movie1);
        ResponseEntity<Movie> movie = moviesController.createMovie(movie2);
        String id = movie.getBody().getId();
        ResponseEntity<Movie> searchMovie = moviesController.getMovieById(id);
        assertNotNull(searchMovie);
        assertEquals(id, searchMovie.getBody().getId());
    }

    @Test
    @DisplayName("Update movie")
    public void updateMovie() {
        Movie movie = Movie.builder().title("2001: A Space Odyssey").year("1968").rated("G").runtime("160 min").genre("Mystery, Sci-Fi")
            .director("Stanley Kubrick").build();
        ResponseEntity<Movie> m = moviesController.createMovie(movie);
        String id = m.getBody().getId();
        movie.setYear("0000");
        ResponseEntity<Movie> updatedMovie = moviesController.updateMovie(movie, id);
        assertNotNull(updatedMovie);
        assertEquals(updatedMovie.getBody().getRated(), "G");
        assertEquals(updatedMovie.getBody().getDirector(), "Stanley Kubrick");
        assertEquals(updatedMovie.getBody().getTitle(), "2001: A Space Odyssey");
        assertEquals(updatedMovie.getBody().getRuntime(), "160 min");
        assertEquals(updatedMovie.getBody().getGenre(), "Mystery, Sci-Fi");
        assertEquals(updatedMovie.getBody().getYear(), "0000");
    }

    @Test
    @DisplayName("Delete movie")
    public void deleteMovie() {
        Movie movie = Movie.builder().title("2001: A Space Odyssey").year("1968").rated("G").runtime("160 min").genre("Mystery, Sci-Fi")
            .director("Stanley Kubrick").build();
        ResponseEntity<Movie> m = moviesController.createMovie(movie);
        assertNotNull(m);
        String id = m.getBody().getId();
        ResponseEntity<HttpStatus> deletedMovie = moviesController.deleteMovie(id);
        assertEquals(HttpStatus.NO_CONTENT, deletedMovie.getStatusCode());
        ResponseEntity<Movie> searchMovie = moviesController.getMovieById(id);
        assertNull(searchMovie.getBody());
    }

    @Test
    @DisplayName("Delete all movies")
    public void deleteAllMovies() {
        Movie movie1 = Movie.builder().title("2001: A Space Odyssey").year("1968").rated("G").runtime("160 min").genre("Mystery, Sci-Fi")
            .director("Stanley Kubrick").build();
        Movie movie2 = Movie.builder().title("The Shining").year("1980").rated("R").runtime("142 min").genre("Horror, Mystery, Thriller")
            .director("Stanley Kubrick").build();
        moviesController.createMovie(movie1);
        moviesController.createMovie(movie2);
        ResponseEntity<HttpStatus> movies = moviesController.deleteAllMovies();
        assertEquals(HttpStatus.NO_CONTENT, movies.getStatusCode());
        ResponseEntity<List<Movie>> allMovies = moviesController.getMovies(null);
        assertEquals(HttpStatus.NO_CONTENT, allMovies.getStatusCode());
        assertNull(allMovies.getBody());
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
