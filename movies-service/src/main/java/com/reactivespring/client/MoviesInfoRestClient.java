package com.reactivespring.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MovieInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

@Component
@Slf4j
public class MoviesInfoRestClient {

    private WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviiesInfoUrl;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieMovieInfo(String movieId) {
        
        String url = moviiesInfoUrl.concat("/{id}");
        RetryBackoffSpec retrySpec = Retry.fixedDelay(3, Duration.ofSeconds(1))
            .filter(ex -> ex instanceof MoviesInfoServerException)
            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> Exceptions.propagate(retrySignal.failure()));

        return webClient
            .get()
            .uri(url, movieId)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, clientResponse -> {

                log.info("Status code is : {}", clientResponse.statusCode().value());
                if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                    return Mono.error(new MovieInfoClientException(
                        "There is no MovieInfo Available for the passed Id : " + movieId,
                        clientResponse.statusCode().value()));
                }

                return clientResponse.bodyToMono(String.class)
                    .flatMap(responseMessage -> {
                        return Mono.error(new MovieInfoClientException(responseMessage, clientResponse.statusCode().value()));
                    });
            })
            .onStatus(HttpStatus::is5xxServerError, clientResponse -> {

                log.info("Status code is : {}", clientResponse.statusCode().value());
                
                return clientResponse.bodyToMono(String.class)
                    .flatMap(responseMessage -> {
                        return Mono.error(new MoviesInfoServerException("Server Exception in MoviesInfoService " + responseMessage));
                    });
            })
            .bodyToMono(MovieInfo.class)
            .retryWhen(retrySpec)
            .log();
    }
}
