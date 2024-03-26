package com.reactivespring.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Review {

    @Id
    private String reviewId;
    @NotNull(message = "rating.movieInfoId : must not be null")
    private Long movieInfoId;
    private String comment;
    @Min(value = 0L, message = "rating.negative : please pass a non-negative value")
    private Double rating;
}
