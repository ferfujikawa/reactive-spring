package com.reactivespring.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class MoviesInfoControllerIntgTest {
    
    private static final String MOVIES_INFO_URL = "/v1/movieinfos";

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        
        List<MovieInfo> movieinfos = Arrays.asList(new MovieInfo(null, "Batman Begins",
                        2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, Arrays.asList("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, Arrays.asList("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));
        
        movieInfoRepository.saveAll(movieinfos)
            .blockLast();
    }

    @AfterEach
    void tearDown() {
        
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {

        //given
        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins1",
                        2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

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
            });

        //then
    }

    @Test
    void getAllMovieInfos() {
        
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
        webTestClient
            .get()
            .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody()
            .jsonPath("$.name").isEqualTo("Dark Knight Rises");
            // .consumeWith(movieInfoEntityExchangeResult -> {
            //     MovieInfo moveiInfo = movieInfoEntityExchangeResult.getResponseBody();
            //     assertNotNull(moveiInfo);
            // });
    }

    @Test
    void updateMovieInfo() {

        //given
        String moveiInfoId = "abc";
        MovieInfo movieInfo = new MovieInfo(null, "Dark Knight Rises1",
                        2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

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

        //when
        webTestClient
            .delete()
            .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
            .exchange()
            .expectStatus()
            .isNoContent();
    }
}
