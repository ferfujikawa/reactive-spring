package com.reactivespring.routes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.reactivespring.domain.Review;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    @MockBean
    private ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    private WebTestClient webTestClient;

    static String REVIEWS_URL = "/v1/reviews";

    @Test
    void addReview() {

        //given
        Review review = new Review(null, 1L, "Awesome Movie", 9.0);

        when(reviewReactiveRepository.save(isA(Review.class)))
            .thenReturn(Mono.just(new Review("abcd", 1L, "Awesome Movie", 9.0)));

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
                assert savedReview != null;
                assert savedReview.getReviewId() != null;
                assertEquals(1L, savedReview.getMovieInfoId());
                assertEquals("Awesome Movie", savedReview.getComment());
                assertEquals(9.0, savedReview.getRating());
            });
    }

    @Test
    void getAllReviews() {

        //given
        List<Review> reviewsList = Arrays.asList(
            new Review(null, 1L, "Awesome Movie", 9.0),
            new Review(null, 1L, "Awesome Movie1", 9.0),
            new Review("123456", 2L, "Excellent Movie", 8.0));

        when(reviewReactiveRepository.findAll())
            .thenReturn(Flux.fromIterable(reviewsList));

        //when
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
        Review review = new Review(null, 1L, "Awesome Movie1 Updated", 9.8);

        when(reviewReactiveRepository.findById(isA(String.class)))
            .thenReturn(Mono.just(new Review()));
        
        when(reviewReactiveRepository.save(isA(Review.class)))
            .thenReturn(Mono.just(new Review(null, 1L, "Awesome Movie1 Updated", 9.8)));

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
                assert updatedReview != null;
                assertEquals(1L, updatedReview.getMovieInfoId());
                assertEquals("Awesome Movie1 Updated", updatedReview.getComment());
            });
    }

    @Test
    void deleteReview() {

        //given
        String reviewId = "123456";

        when(reviewReactiveRepository.findById(isA(String.class)))
            .thenReturn(Mono.just(new Review()));
        
        when(reviewReactiveRepository.deleteById(isA(String.class)))
            .thenReturn(Mono.empty());

        //when
        webTestClient
            .delete()
            .uri(REVIEWS_URL + "/{id}", reviewId)
            .exchange()
            .expectStatus()
            .isNoContent();
    }
}
