package com.github.ivanmarban.movies.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "movies")
public class Movie {

    @Id
    private String id;
    private String title;
    private String year;
    private String rated;
    private String runtime;
    private String genre;
    private String director;

}
