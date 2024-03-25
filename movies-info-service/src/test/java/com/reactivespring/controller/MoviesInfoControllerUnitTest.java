package com.reactivespring.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService moviesInfoServiceMock;

    static String MOVIES_INFO_URL = "/v1/movieinfos";

    @Test
    void getAllMoviesInfo() {

        List<MovieInfo> movieinfos = Arrays.asList(
            new MovieInfo(null, "Batman Begins",
                    2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
            new MovieInfo(null, "The Dark Knight",
                    2008, Arrays.asList("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
            new MovieInfo("abc", "Dark Knight Rises",
                    2012, Arrays.asList("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        when(moviesInfoServiceMock.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieinfos));

        webTestClient
            .get()
            .uri(MOVIES_INFO_URL)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBodyList(MovieInfo.class)
            .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        
        String movieInfoId = "abc";
        MovieInfo movieInfo = new MovieInfo("abc", "Dark Knight Rises", 2012, Arrays.asList("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(moviesInfoServiceMock.getMovieInfoById(movieInfoId)).thenReturn(Mono.just(movieInfo));

        webTestClient
            .get()
            .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody()
            .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    void addMovieInfo() {

        //given
        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins1",
            2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(
            Mono.just(new MovieInfo("mockId", "Batman Begins1",
                2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")))
        );

        //when
        webTestClient
            .post()
            .uri(MOVIES_INFO_URL)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(MovieInfo.class)
            .consumeWith(movieInfoEntityExchangeResult -> {

                MovieInfo savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                assert savedMovieInfo!=null;
                assert savedMovieInfo.getMovieInfoId()!=null;
                assertEquals("mockId", savedMovieInfo.getMovieInfoId());
            });

        //then
    }

    @Test
    void updateMovieInfo() {

        //given
        String moveiInfoId = "abc";
        MovieInfo movieInfo = new MovieInfo(null, "Dark Knight Rises1",
            2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(moviesInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(
            Mono.just(new MovieInfo(moveiInfoId, "Dark Knight Rises1",
                2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")))
        );

        //when
        webTestClient
            .put()
            .uri(MOVIES_INFO_URL + "/{id}", moveiInfoId)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(MovieInfo.class)
            .consumeWith(movieInfoEntityExchangeResult -> {

                MovieInfo updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                assert updatedMovieInfo!=null;
                assert updatedMovieInfo.getMovieInfoId()!=null;
                assertEquals("Dark Knight Rises1", updatedMovieInfo.getName());
            });

        //then
    }

    @Test
    void deleteMovieInfo() {

        //given
        String movieInfoId = "abc";

        when(moviesInfoServiceMock.deleteMovieInfo(isA(String.class))).thenReturn(Mono.empty());

        //when
        webTestClient
            .delete()
            .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
            .exchange()
            .expectStatus()
            .isNoContent();
    }

    @Test
    void addMovieInfo_validation() {

        //given
        MovieInfo movieInfo = new MovieInfo(null, "",
            -2005, Arrays.asList(""), LocalDate.parse("2005-06-15"));

        //when
        webTestClient
            .post()
            .uri(MOVIES_INFO_URL)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(String.class)
            .consumeWith(stringEntityExchangeResult -> {

                String responseBody = stringEntityExchangeResult.getResponseBody();
                String expectedErrorMessage = "movieInfo.cast must be present,movieInfo.name must be present,movieInfo.year must be a Positive value";
                assertEquals(expectedErrorMessage, responseBody);
            });

        //then
    }
}
