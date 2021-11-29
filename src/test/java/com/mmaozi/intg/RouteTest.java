package com.mmaozi.intg;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class RouteTest extends BaseTest {

    @Test
    void should_return_404_while_path_not_exists() {
        given()
                .get("/")
                .then()
                .statusCode(404);
    }

    @Test
    void should_return_200_while_path_query_example_customer() {
        given()
                .get("/customers/123")
                .then()
                .statusCode(200)
                .body("customerId", Matchers.equalTo(123))
                .extract().body().asString();
    }

    @Test
    void should_return_200_while_path_query_complex_nested_route() {
        given()
                .get("/customers/123/orders/1")
                .then()
                .statusCode(200)
                .body("orderId", Matchers.equalTo(1))
                .extract().body().asString();
    }

    @Test
    void should_return_500_while_internal_error() {
        given()
                .get("/customers/123/orders/a/items")
                .then()
                .statusCode(500)
                .extract().body().asString();
    }
}
