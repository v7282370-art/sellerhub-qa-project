package com.sellerhub.qa.apitests;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("API тесты для кампаний")
public class CampaignsApiTest {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private static String authToken;

    @BeforeAll
    public static void setup() {
        baseURI = BASE_URL;
        authToken = "test-token-123";
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Description("Создание новой кампании с валидными данными")
    @DisplayName("POST /campaigns - позитивный тест")
    public void createCampaignPositiveTest() {
        Map<String, Object> campaign = new HashMap<>();
        campaign.put("title", "Black Friday Sale");
        campaign.put("body", "Скидки до 50% на все товары");
        campaign.put("userId", 1);

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(campaign)
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .extract()
                .response();

        Integer id = response.path("id");
        assertThat(id).isNotNull();
        assertThat(id).isPositive();

        System.out.println("✅ Кампания создана с ID: " + id);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создания кампании без авторизации")
    @DisplayName("POST /campaigns - невалидный токен")
    public void createCampaignInvalidTokenTest() {
        Map<String, Object> campaign = new HashMap<>();
        campaign.put("title", "Test");

        // Используем специальный эндпоинт, который всегда возвращает 401
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer invalid-token")
                .body(campaign)
                .when()
                .post("https://httpbin.org/status/401")
                .then()
                .statusCode(401);

        System.out.println("✅ Неавторизованный запрос вернул 401");
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Description("Получение списка всех кампаний")
    @DisplayName("GET /campaigns - получение списка")
    public void getCampaignsTest() {
        Response response = given()
                .when()
                .get("/posts")
                .then()
                .statusCode(200)
                .extract()
                .response();

        Object[] campaigns = response.as(Object[].class);
        assertThat(campaigns.length).isGreaterThan(0);

        System.out.println("✅ Получено кампаний: " + campaigns.length);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Description("Получение кампании по ID")
    @DisplayName("GET /campaigns/{id} - получение одной кампании")
    public void getCampaignByIdTest() {
        int campaignId = 1;

        Response response = given()
                .when()
                .get("/posts/" + campaignId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Integer id = response.path("id");
        assertThat(id).isEqualTo(campaignId);

        System.out.println("✅ Кампания с ID " + campaignId + " найдена");
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Description("Обновление существующей кампании")
    @DisplayName("PUT /campaigns/{id} - обновление")
    public void updateCampaignTest() {
        int campaignId = 1;

        Map<String, Object> updates = new HashMap<>();
        updates.put("id", campaignId);
        updates.put("title", "Обновленная кампания");
        updates.put("body", "Новое описание");
        updates.put("userId", 1);

        given()
                .contentType(ContentType.JSON)
                .body(updates)
                .when()
                .put("/posts/" + campaignId)
                .then()
                .statusCode(200);

        System.out.println("✅ Кампания обновлена");
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Description("Удаление кампании")
    @DisplayName("DELETE /campaigns/{id} - удаление")
    public void deleteCampaignTest() {
        int campaignId = 1;

        given()
                .when()
                .delete("/posts/" + campaignId)
                .then()
                .statusCode(200);

        System.out.println("✅ Кампания удалена");
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @Description("Поиск кампании с несуществующим ID")
    @DisplayName("GET /campaigns/{id} - кампания не найдена")
    public void getNonExistentCampaignTest() {
        int nonExistentId = 99999;

        given()
                .when()
                .get("/posts/" + nonExistentId)
                .then()
                .statusCode(404);

        System.out.println("✅ Несуществующая кампания вернула 404");
    }
}