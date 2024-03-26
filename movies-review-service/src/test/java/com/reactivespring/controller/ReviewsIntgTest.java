package com.reactivespring.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
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
import org.springframework.web.util.UriComponentsBuilder;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

	@Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    static String REVIEWS_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {
        
        List<Review> reviewsList = Arrays.asList(
            new Review(null, 1L, "Awesome Movie", 9.0),
            new Review(null, 1L, "Awesome Movie1", 9.0),
            new Review("123456", 2L, "Excellent Movie", 8.0));
        reviewReactiveRepository.saveAll(reviewsList)
            .blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void addReview() {

        //given
        Review review = new Review(null, 1L, "Awesome Movie", 9.0);

        //when
        webTestClient
            .post()
            .uri(REVIEWS_URL)
            .bodyValue(review)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Review.class)
            .consumeWith(reviewEntityExchangeResult -> {

                Review savedReview = reviewEntityExchangeResult.getResponseBody();
                assert savedReview!= null;
                assert savedReview.getReviewId() != null;
            });
    }

    @Test
    void getAllReviews() {
        
        webTestClient
            .get()
            .uri(REVIEWS_URL)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBodyList(Review.class)
            .hasSize(3);
    }

    @Test
    void updateReview() {

        //given
        String reviewId = "123456";
        Review review = new Review(null, 1L, "Awsome Movie Updated", 8.8);

        //when
        webTestClient
            .put()
            .uri(REVIEWS_URL + "/{id}", reviewId)
            .bodyValue(review)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(Review.class)
            .consumeWith(reviewEntityExchangeResult -> {

                Review updatedReview = reviewEntityExchangeResult.getResponseBody();
                assert updatedReview!=null;
                assertEquals("Awsome Movie Updated", updatedReview.getComment());
                assertEquals(8.8, updatedReview.getRating());
            });

        //then
    }

    @Test
    void deleteReview() {

        //given
        String reviewId = "123456";

        //when
        webTestClient
            .delete()
            .uri(REVIEWS_URL + "/{id}", reviewId)
            .exchange()
            .expectStatus()
            .isNoContent();
    }

    @Test
    void getReviewsByMovieInfoId() {
        
        //given
        URI uri = UriComponentsBuilder.fromUriString(REVIEWS_URL)
            .queryParam("movieInfoId", 1L)
            .buildAndExpand().toUri();

        webTestClient
            .get()
            .uri(uri)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBodyList(Review.class)
            .hasSize(2);
    }
}
