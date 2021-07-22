package com.github.ivanmarban.movies.repository;

import com.github.ivanmarban.movies.model.Movie;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MoviesRepository extends MongoRepository<Movie, String> {

    List<Movie> findByTitleContaining(String title);

}
