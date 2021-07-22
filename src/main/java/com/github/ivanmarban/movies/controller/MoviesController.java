package com.github.ivanmarban.movies.controller;

import com.github.ivanmarban.movies.model.Movie;
import com.github.ivanmarban.movies.repository.MoviesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Slf4j
public class MoviesController {

    private final MoviesRepository moviesRepository;

    public MoviesController(MoviesRepository moviesRepository) {
        this.moviesRepository = moviesRepository;
    }

    @GetMapping("/movies")
    public ResponseEntity<List<Movie>> getMovies(@RequestParam(required = false) String title) {
        try {
            List<Movie> movies = new ArrayList<>();
            if (title == null) {
                movies.addAll(moviesRepository.findAll());
            } else {
                movies.addAll(moviesRepository.findByTitleContaining(title));
            }
            if (movies.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(movies, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting movies.", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable("id") String id) {
        Optional<Movie> movieData = moviesRepository.findById(id);
        return movieData.map(movie -> new ResponseEntity<>(movie, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/movies")
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        try {
            Movie m = moviesRepository.save(Movie.builder().title(movie.getTitle()).year(movie.getYear()).genre(movie.getGenre())
                .director(movie.getDirector()).rated(movie.getRated()).rated(movie.getRated()).runtime(movie.getRuntime()).build());
            return new ResponseEntity<>(m, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating movie.", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/movies/{id}")
    public ResponseEntity<Movie> updateMovie(@RequestBody Movie movie, @PathVariable("id") String id) {
        Optional<Movie> movieData = moviesRepository.findById(id);
        if (movieData.isPresent()) {
            Movie m = movieData.get();
            m.setRated(movie.getRated());
            m.setGenre(movie.getGenre());
            m.setDirector(movie.getDirector());
            m.setRuntime(movie.getRuntime());
            m.setTitle(movie.getTitle());
            m.setYear(movie.getYear());
            return new ResponseEntity<>(moviesRepository.save(m), HttpStatus.OK);
        } else {
            log.warn("Movie {} not found.", movie);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/movies/{id}")
    public ResponseEntity<HttpStatus> deleteMovie(@PathVariable("id") String id) {
        try {
            moviesRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("Error deleting movie.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/movies")
    public ResponseEntity<HttpStatus> deleteAllMovies() {
        try {
            moviesRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("Error deleting all movies.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
