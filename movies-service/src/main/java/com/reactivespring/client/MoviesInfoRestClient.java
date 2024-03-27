package com.reactivespring.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.reactivespring.domain.MovieInfo;

import reactor.core.publisher.Mono;

@Component
public class MoviesInfoRestClient {

    private WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviiesInfoUrl;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieMovieInfo(String movieId) {
        
        String url = moviiesInfoUrl.concat("/{id}");
        return webClient
            .get()
            .uri(url, movieId)
            .retrieve()
            .bodyToMono(MovieInfo.class)
            .log();
    }
}
