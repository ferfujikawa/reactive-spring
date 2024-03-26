package com.reactivespring.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import com.reactivespring.domain.MovieInfo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataMongoTest
@ActiveProfiles("test")
public class MovieInfoRepositoryIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

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
    void findAll() {
        
        //given

        //when
        Flux<MovieInfo> moviesInfoFlux = movieInfoRepository.findAll().log();

        //then
        StepVerifier.create(moviesInfoFlux)
            .expectNextCount(3)
            .verifyComplete();
    }

    @Test
    void findById() {
        
        //given

        //when
        Mono<MovieInfo> moviesInfoMono = movieInfoRepository.findById("abc").log();

        //then
        StepVerifier.create(moviesInfoMono)
            .assertNext(movieInfo -> {
                assertEquals("Dark Knight Rises", movieInfo.getName());
            })
            .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        
        //given

        //when
        Mono<MovieInfo> moviesInfoMono = movieInfoRepository.save(new MovieInfo(null, "Batman Begins1",
        2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")));

        //then
        StepVerifier.create(moviesInfoMono)
            .assertNext(movieInfo1 -> {
                assertNotNull(movieInfo1.getMovieInfoId());
                assertEquals("Batman Begins1", movieInfo1.getName());
            })
            .verifyComplete();
    }

    @Test
    void updateMovieInfo() {
        
        //given
        MovieInfo movieInfo = movieInfoRepository.findById("abc").block();
        movieInfo.setYear(2021);

        //when
        Mono<MovieInfo> moviesInfoMono = movieInfoRepository.save(movieInfo);

        //then
        StepVerifier.create(moviesInfoMono)
            .assertNext(movieInfo1 -> {
                assertEquals(2021, movieInfo1.getYear());
            })
            .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        
        //given

        //when
        movieInfoRepository.deleteById("abc").block();
        Flux<MovieInfo> moviesInfoFlux = movieInfoRepository.findAll().log();

        //then
        StepVerifier.create(moviesInfoFlux)
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void findByYear() {
        
        //given

        //when
        Flux<MovieInfo> moviesInfoFlux = movieInfoRepository.findByYear(2005).log();

        //then
        StepVerifier.create(moviesInfoFlux)
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void findByName() {
        
        //given

        //when
        Mono<MovieInfo> moviesInfoMono = movieInfoRepository.findOneByName("Dark Knight Rises").log();

        //then
        StepVerifier.create(moviesInfoMono)
            .assertNext(movieInfo -> {
                assertEquals("Dark Knight Rises", movieInfo.getName());
            })
            .verifyComplete();
    }
}
