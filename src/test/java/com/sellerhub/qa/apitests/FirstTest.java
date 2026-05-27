package com.sellerhub.qa.apitests;

import org.junit.jupiter.api.Test;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class FirstTest {

    @Test
    @Description("Проверяем, что Google отвечает с кодом 200")
    @Severity(SeverityLevel.CRITICAL)
    public void testGoogleIsAvailable() {
        int statusCode = given()
                .when()
                .get("https://www.google.com")
                .getStatusCode();

        assertThat(statusCode).isEqualTo(404);
    }
}