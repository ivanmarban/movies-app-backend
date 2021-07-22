package com.github.ivanmarban.movies.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ivanmarban.movies.model.Movie;
import com.github.ivanmarban.movies.repository.MoviesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("MoviesController Unit Tests")
@WebMvcTest(MoviesController.class)
public class MoviesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MoviesRepository moviesRepository;

    @Test
    @DisplayName("Create movie")
    public void createMovie() throws Exception {

        Movie movie = Movie.builder().title("2001: A Space Odyssey").year("1968").rated("G").runtime("160 min").genre("Mystery, Sci-Fi")
            .director("Stanley Kubrick").build();

        given(moviesRepository.save(movie)).willReturn(movie);

        mockMvc.perform(MockMvcRequestBuilders
            .post("/api/movies")
            .content(asJsonString(movie))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("2001: A Space Odyssey"));
    }

    @Test
    @DisplayName("Create movie. Throws exception.")
    public void createMovieThrowsException() throws Exception {

        Movie movie = Movie.builder().title("2001: A Space Odyssey").year("1968").rated("G").runtime("160 min").genre("Mystery, Sci-Fi")
            .director("Stanley Kubrick").build();

        willThrow(new RuntimeException("horror")).given(moviesRepository).save(movie);

        mockMvc.perform(MockMvcRequestBuilders
            .post("/api/movies")
            .content(asJsonString(movie))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Get all movies")
    public void getMovies() throws Exception {

        given(moviesRepository.findAll()).willReturn(movieList());

        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/movies")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Get all movies. Not found.")
    public void getMoviesNotFound() throws Exception {

        given(moviesRepository.findAll()).willReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/movies")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Get all movies. Throws exception.")
    public void getMoviesThrowsException() throws Exception {

        willThrow(new RuntimeException("horror")).given(moviesRepository).findAll();

        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/movies")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Get movie by title containing 'Space'")
    public void getMovieByTitle() throws Exception {

        List<Movie> movies = new ArrayList<>();
        movies.add(Movie.builder().id("60f6cd5366949871c415d9fd").title("2001: A Space Odyssey").year("1968").rated("G").runtime("160 min")
            .genre("Mystery, Sci-Fi").director("Stanley Kubrick").build());

        given(moviesRepository.findByTitleContaining("Space")).willReturn(movies);

        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/movies")
            .param("title", "Space")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("2001: A Space Odyssey"));
    }

    @Test
    @DisplayName("Get movie by ID")
    public void getMovieById() throws Exception {
        Movie movie = Movie.builder().id("60f6cd5366949871c415d9fd").title("2001: A Space Odyssey").year("1968").rated("G").runtime("160 min")
            .genre("Mystery, Sci-Fi").director("Stanley Kubrick").build();

        given(moviesRepository.findById("60f6cd5366949871c415d9fd")).willReturn(Optional.ofNullable(movie));

        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/movies/{id}", "60f6cd5366949871c415d9fd")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("60f6cd5366949871c415d9fd"));
    }

    @Test
    @DisplayName("Get movie by ID. Not Found.")
    public void getMovieByIdNotFound() throws Exception {

        given(moviesRepository.findById("60f6cd5366949871c415d9fd")).willReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/movies/{id}", "60f6cd5366949871c415d9fd")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update movie")
    public void updateMovie() throws Exception {
        Movie movie = Movie.builder().id("60f6cd5366949871c415d9fd").title("2001: A Space Odyssey").year("1968").rated("G").runtime("160 min")
            .genre("Mystery, Sci-Fi").director("Stanley Kubrick").build();

        Movie updatedMovie = Movie.builder().id("60f6cd5366949871c415d9fd").title("2001: A Space Odyssey").year("0000").rated("G").runtime("160 min")
            .genre("Mystery, Sci-Fi").director("Stanley Kubrick").build();

        given(moviesRepository.save(movie)).willReturn(updatedMovie);
        given(moviesRepository.findById("60f6cd5366949871c415d9fd")).willReturn(Optional.of(movie));

        mockMvc.perform(MockMvcRequestBuilders
            .put("/api/movies/{id}", "60f6cd5366949871c415d9fd")
            .content(asJsonString(movie))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.year").value("0000"));
    }

    @Test
    @DisplayName("Update movie. Not Found.")
    public void updateMovieNotFound() throws Exception {
        Movie movie = Movie.builder().id("60f6cd5366949871c415d9fd").title("2001: A Space Odyssey").year("1968").rated("G").runtime("160 min")
            .genre("Mystery, Sci-Fi").director("Stanley Kubrick").build();

        given(moviesRepository.findById("60f6cd5366949871c415d9fd")).willReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders
            .put("/api/movies/{id}", "60f6cd5366949871c415d9fd")
            .content(asJsonString(movie))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete movie")
    public void deleteMovie() throws Exception {

        willDoNothing().given(moviesRepository).deleteById("60f6cd5366949871c415d9fd");

        mockMvc.perform(MockMvcRequestBuilders
            .delete("/api/movies/{id}", "60f6cd5366949871c415d9fd"))
            .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("Delete movie. Throws exception.")
    public void deleteMovieThrowsException() throws Exception {

        willThrow(new RuntimeException("horror")).given(moviesRepository).deleteById("60f6cd5366949871c415d9fd");

        mockMvc.perform(MockMvcRequestBuilders
            .delete("/api/movies/{id}", "60f6cd5366949871c415d9fd"))
            .andExpect(status().isInternalServerError());

    }

    @Test
    @DisplayName("Delete all movies")
    public void deleteAllMovies() throws Exception {

        willDoNothing().given(moviesRepository).deleteAll();

        mockMvc.perform(MockMvcRequestBuilders
            .delete("/api/movies"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Delete all movies. Throws exception.")
    public void deleteAllMoviesThrowsException() throws Exception {

        willThrow(new RuntimeException("horror")).given(moviesRepository).deleteAll();

        mockMvc.perform(MockMvcRequestBuilders
            .delete("/api/movies"))
            .andExpect(status().isInternalServerError());
    }

    public List<Movie> movieList() {
        List<Movie> movies = new ArrayList<>();
        movies.add(Movie.builder().id("60f6cd5366949871c415d9fd").title("2001: A Space Odyssey").year("1968").rated("G").runtime("160 min")
            .genre("Mystery, Sci-Fi").director("Stanley Kubrick").build());
        movies.add(Movie.builder().id("5effaa5662679b5af2c58829").title("The Shining").year("1980").rated("R").runtime("142 min")
            .genre("Horror, Mystery, Thriller").director("Stanley Kubrick").build());
        return movies;
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
