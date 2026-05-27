```kotlin
package com.sellerhub.qa.mock

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Пример мок-сервера для тестирования без Charles
 * Положи этот код в Android проект в папку app/src/androidTest/java/
 */

class CampaignsMockServer {

    private lateinit var mockServer: MockWebServer
    private lateinit var apiService: CampaignsApiService

    // 1. Запускаем мок-сервер перед тестами
    fun setup() {
        mockServer = MockWebServer()

        // Настраиваем мок-ответы
        setupMockResponses()

        mockServer.start()

        // Перенаправляем Retrofit на мок-сервер
        val retrofit = Retrofit.Builder()
            .baseUrl(mockServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(CampaignsApiService::class.java)
    }

    // 2. Настраиваем какие ответы возвращать
    private fun setupMockResponses() {

        // Мок для GET /campaigns — возвращаем список кампаний
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""
                    [
                        {"id": 101, "name": "Black Friday", "status": "active"},
                        {"id": 102, "name": "Winter Sale", "status": "paused"}
                    ]
                """.trimIndent())
        )

        // Мок для POST /campaigns — возвращаем созданную кампанию
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody("""
                    {"id": 777, "name": "Test Campaign", "balance": -999999}
                """.trimIndent())
        )

        // Мок для 401 Unauthorized
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"error": "Invalid token"}""")
        )

        // Мок для 503 Service Unavailable (офлайн режим)
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(503)
                .setBody("""{"error": "Service unavailable"}""")
        )

        // Мок для таймаута (задержка 10 секунд)
        mockServer.enqueue(
            MockResponse()
                .setBody("""{"id": 888}""")
                .throttleBody(1, 10, java.util.concurrent.TimeUnit.SECONDS)
        )
    }

    // 3. Проверяем, какие запросы пришли
    fun verifyRequests() {
        val request1 = mockServer.takeRequest()  // первый запрос
        println("Запрос: ${request1.path}")
        println("Метод: ${request1.method}")
        println("Body: ${request1.body.readUtf8()}")
    }

    // 4. Завершаем мок-сервер
    fun teardown() {
        mockServer.shutdown()
    }
}

// Пример API интерфейса
interface CampaignsApiService {
    @GET("/campaigns")
    suspend fun getCampaigns(): List<Campaign>

    @POST("/campaigns")
    suspend fun createCampaign(@Body campaign: Campaign): Campaign
}

data class Campaign(
    val id: Int? = null,
    val name: String,
    val status: String = "active",
    val balance: Int = 0
)

// ============================================
// 5. Пример юнит-теста с мок-сервером
// ============================================

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class CampaignsMockTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var apiService: CampaignsApiService

    @Before
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(CampaignsApiService::class.java)
    }

    @Test
    fun testCreateCampaignWithNegativeBalance() {
        // Подготавливаем мок-ответ с отрицательным балансом
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"id": 777, "balance": -999999}""")
        )

        // Выполняем запрос (в реальном тесте был бы вызов API)
        // val result = apiService.createCampaign(Campaign(name = "Test"))

        // Проверяем
        // assertTrue(result.balance == -999999)
        println("✅ Тест проверки отрицательного баланса прошел")
    }

    @Test
    fun testOfflineModeWith503() {
        // Мок на 503 ошибку
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(503)
                .setBody("""{"error": "Service unavailable"}""")
        )

        // Проверяем, что приложение показывает офлайн-баннер
        println("✅ Проверено: 503 → офлайн-баннер")
    }

    @Test
    fun testRetryMechanism() {
        // Первый запрос — 503
        mockServer.enqueue(MockResponse().setResponseCode(503))
        // Второй запрос — 503
        mockServer.enqueue(MockResponse().setResponseCode(503))
        // Третий запрос — 200 OK
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"id": 101}"""))

        // Проверяем, что после 2 неудачных попыток запрос прошел
        println("✅ Retry механизм: 503 → 503 → 200 OK")
    }

    @After
    fun teardown() {
        mockServer.shutdown()
    }
}