# reactive-spring

Demo project from [Build Reactive MicroServices using Spring WebFlux/SpringBoot](https://www.udemy.com/course/build-reactive-restful-apis-using-spring-boot-webflux) course.

## How to run the application

Just start the containers by running the following command:

```bash
docker compose up
```

## Application Endpoints

Import [reactive-spring.postman_collection.json](reactive-spring.postman_collection.json) file on [Postman](https://www.postman.com/).

## Architecture Explanation

The solution is a composition of three services wich uses [Spring Boot WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html) library to expose non-blocking endpoints:

- **Movies Info Service**: REST API that handles general information about movies.
- **Movies Review Service**: Functional API that handles movies' reviews.
- **Movies Service**: REST API that consumes prior listened services and join movies information with respective reviews in a GET endpoint.
