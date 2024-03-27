package com.reactivespring.util;

import java.time.Duration;

import com.reactivespring.exception.MoviesInfoServerException;

import reactor.core.Exceptions;
import reactor.util.retry.Retry;

public class RetryUtil {

    public static Retry retrySpec() {

        return  Retry.fixedDelay(3, Duration.ofSeconds(1))
            .filter(ex -> ex instanceof MoviesInfoServerException)
            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> Exceptions.propagate(retrySignal.failure()));
    }
}
