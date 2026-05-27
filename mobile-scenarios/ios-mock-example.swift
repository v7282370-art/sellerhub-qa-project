import Foundation
import OHHTTPStubs
import OHHTTPStubsSwift

// ============================================
// 1. URLProtocol — нативный способ моков
// ============================================

class MockURLProtocol: URLProtocol {

    override class func canInit(with request: URLRequest) -> Bool {
        // Перехватываем все запросы к /campaigns
        return request.url?.path.contains("/campaigns") == true
    }

    override class func canonicalRequest(for request: URLRequest) -> URLRequest {
        return request
    }

    override func startLoading() {
        // Создаем мок-ответ
        let mockResponse = """
        {
            "id": 777,
            "name": "Mock Campaign",
            "balance": -999999,
            "status": "active"
        }
        """

        let data = mockResponse.data(using: .utf8)
        let url = request.url!
        let response = HTTPURLResponse(
            url: url,
            statusCode: 200,
            httpVersion: nil,
            headerFields: ["Content-Type": "application/json"]
        )

        // Отправляем ответ
        client?.urlProtocol(self, didReceive: response!, cacheStoragePolicy: .notAllowed)
        client?.urlProtocol(self, didLoad: data!)
        client?.urlProtocolDidFinishLoading(self)
    }

    override func stopLoading() {}
}

// ============================================
// 2. OHHTTPStubs — удобная библиотека
// ============================================

class CampaignsMockStubs {

    static func setupMocks() {
        // Мок для GET /campaigns — список кампаний
        stub(condition: isPath("/campaigns") && isMethodGET()) { _ in
            let stubData = """
            [
                {"id": 101, "name": "Black Friday", "status": "active"},
                {"id": 102, "name": "Winter Sale", "status": "paused"}
            ]
            """.data(using: .utf8)!

            return HTTPStubsResponse(data: stubData, statusCode: 200, headers: nil)
                .responseTime(0.5) // задержка 0.5 сек
        }

        // Мок для POST /campaigns — создание кампании
        stub(condition: isPath("/campaigns") && isMethodPOST()) { _ in
            let stubData = """
            {"id": 777, "name": "New Campaign", "balance": -999999}
            """.data(using: .utf8)!

            return HTTPStubsResponse(data: stubData, statusCode: 201, headers: nil)
        }

        // Мок для 503 ошибки (офлайн режим)
        stub(condition: isPath("/sync")) { _ in
            let error = NSError(domain: NSURLErrorDomain, code: URLError.networkConnectionLost.rawValue)
            return HTTPStubsResponse(error: error)
        }

        // Мок с задержкой 10 секунд (симуляция таймаута)
        stub(condition: isPath("/slow-api")) { _ in
            let stubData = "{\"status\": \"ok\"}".data(using: .utf8)!
            return HTTPStubsResponse(data: stubData, statusCode: 200, headers: nil)
                .responseTime(10) // 10 секунд задержки
        }
    }

    static func removeMocks() {
        HTTPStubs.removeAllStubs()
    }
}

// ============================================
// 3. .xcconfig — переключение окружений
// ============================================

/*
 Создай файл Debug.xcconfig:

 API_URL = https://mock.api.sellerhub.com
 USE_MOCKS = YES

 Создай файл Release.xcconfig:

 API_URL = https://api.sellerhub.com
 USE_MOCKS = NO
 */

// В коде используй:
// let apiURL = Bundle.main.infoDictionary?["API_URL"] as? String ?? ""

// ============================================
// 4. Пример теста с моками
// ============================================

import XCTest

class CampaignsMockTests: XCTestCase {

    override func setUp() {
        super.setUp()
        // Включаем моки для тестов
        CampaignsMockStubs.setupMocks()
    }

    override func tearDown() {
        CampaignsMockStubs.removeMocks()
        super.tearDown()
    }

    func testNegativeBalanceHandling() {
        // Ожидаем, что приложение корректно покажет отрицательный баланс
        let expectation = XCTestExpectation(description: "API called")

        // Здесь был бы реальный запрос к API
        // В моке баланс = -999999

        // Проверяем, что UI не упал
        // и отображается красный текст "Balance: -999999"

        expectation.fulfill()
        wait(for: [expectation], timeout: 2)
    }

    func testOfflineModeWith503() {
        let expectation = XCTestExpectation(description: "Offline banner shown")

        // Проверяем:
        // 1. При 503 появляется офлайн-баннер
        // 2. Кнопки становятся неактивными
        // 3. Через 10 секунд реконнект

        expectation.fulfill()
        wait(for: [expectation], timeout: 2)
    }
}