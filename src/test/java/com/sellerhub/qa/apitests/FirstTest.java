package com.sellerhub.qa.apitests;

import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class FirstTest {

    @Test
    public void testGoogleIsAvailable() {
        // Простой тест, который проверяет, что Google отвечает
        int statusCode = given()
                .when()
                .get("https://www.google.com")
                .getStatusCode();

        assertThat(statusCode).isEqualTo(200);

        System.out.println("Тест прошел! Статус код: " + statusCode);
    }
}