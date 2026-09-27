# Shawarma AQA Showcase

Демонстрационный AQA-фреймворк автотестов для демо-приложения Shawarma Shop.
Демонстрирует подход к построению масштабируемого AQA-фреймворка: API + UI + контракты партнёров + Kafka + БД.

[![CI](https://github.com/MaksimSvyat/shawarma-aqa-showcase/actions/workflows/ci.yml/badge.svg)](https://github.com/MaksimSvyat/shawarma-aqa-showcase/actions/workflows/ci.yml)
[![Allure Report](https://img.shields.io/badge/Allure-Report-blue)](https://MaksimSvyat.github.io/shawarma-aqa-showcase/)

## Что это

Фреймворк для тестирования приложения Shawarma Shop — сервиса заказа шаурмы. Приложение состоит из backend (Spring Boot), frontend (React) и набора внешних партнёров: платёжный шлюз (REST), поставщик мяса (SOAP), пекарня (gRPC), сервис отзывов (GraphQL).

Фреймворк покрывает:

- UI-тесты (Selenide + Chrome)
- API/E2E-тесты (REST Assured)
- Интеграционные тесты с партнёрами через WireMock: SOAP, gRPC, GraphQL, REST
- Kafka-события (consumer в тестах)
- Проверки состояния БД (PostgreSQL через JDBC + HikariCP)
- Отчёты Allure с шагами, скриншотами, логами

## Стек

- Язык: Java 21
- Тест-раннер: JUnit 5
- Assertions: AssertJ
- UI: Selenide 7
- API: REST Assured 6
- Моки партнёров: WireMock 3 (REST + SOAP + GraphQL + gRPC)
- Kafka: kafka-clients
- БД: PostgreSQL + HikariCP + JDBC
- Отчёты: Allure
- Конфиги: Owner
- Сборка: Gradle
- Инфраструктура: Docker Compose

## Структура проекта

```
src/test/java/com/shawarmashop/tests
├── auth/           — ApiClient, Credentials, Users
├── db/             — JDBC-обёртка, репозитории, фикстуры, scope-очистка
├── dto/            — DTO всех протоколов
├── e2e/
│   ├── showcase/   — E2E-тесты
│   │   └── mocks/  — showcase-тесты SOAP/gRPC/GraphQL
│   └── support/    — helpers, utils
├── env/            — TestEnvironment (Owner)
├── extensions/     — @DbTest, @KafkaTest, @IntegrationsTest
├── integrations/   — WireMock DSL + stubs партнёров
├── kafka/          — KafkaTestBus, топики, события
├── rest/           — BaseClient, ApiResult, REST-клиенты
├── support/        — Json
└── ui/             — Selenide, страницы, компоненты, extensions
```

## Быстрый старт

Требования:

- JDK 21
- Docker + Docker Compose v2
- Chrome (для UI-тестов)

Запуск:

```
# 0. Клонировать репозиторий и проверить, что порты 5434, 8081, 8082, 8083, 9092, 3001 свободны

# 1. Поднять локальный стенд
docker compose up -d

# 2. Дождаться healthy-статуса
docker compose ps

# 3. Прогнать API/E2E-тесты
./gradlew test --no-daemon --tests 'com.shawarmashop.tests.e2e.showcase.*'

# 4. Прогнать UI-тесты
./gradlew test --no-daemon --tests 'com.shawarmashop.tests.ui.tests.*'
```

Allure-отчёт:

```
./gradlew allureServe
```

## Что демонстрирует проект

Мультипротокольность. Один фреймворк работает с четырьмя протоколами через WireMock:

- REST — платежи (/payments/charge)
- SOAP — поставщик мяса (/soap/meat)
- gRPC — пекарня (BreadBakery/orderBatch)
- GraphQL — сервис отзывов (/graphql)

Kafka-события. KafkaTestBus с отдельной consumer-group на тест-класс:

```
OrderEventsTopic events = kafka.orders();
OrderEvent placed = events.waitPlaced(orderId);
```

Проверка состояния БД. Fixture-слой с автоматической очисткой через ThreadLocal-scope:

```
RecipeRow recipe = RecipeFixture.builder()
        .name("TEST_RECIPE")
        .price(199.0)
        .build()
        .insert();
// cleanup произойдёт автоматически после теста
```

Кастомные JUnit extensions:

```
@DbTest
@KafkaTest
@IntegrationsTest
class OrderLifecycleE2ETest { ... }
```

Каждая аннотация подключает свой слой и автоматически чистит за собой.

Перехват HTTP-ответов в UI-тестах. BaseUITest.interceptResponse позволяет проверить реальный ответ API, который фронт получил:

```
OrderResponse response = interceptResponse(
        HttpMethod.POST,
        "/api/v1/orders",
        OrderResponse.class,
        orderModal::submit
);
```

Allure:

- @Step на всех helper-методах
- AllureRestAssured для API-запросов
- AllureSelenide для UI-шагов, скриншотов, page-source
- AllureExtension добавляет Cookies, LocalStorage, Performance Logs при падении теста

## Примеры тестов

E2E: полный lifecycle заказа

```
@DbTest
@KafkaTest
@IntegrationsTest
@DisplayName("E2E: заказ PENDING → PAID → PREPARING → DONE")
class OrderLifecycleE2ETest {

    @Test
    void fullLifecycle(IntegrationStubs stubs, KafkaTestBus kafka) {
        OrderResponse created = orderHelper.createOrder(recipe.getId(), 1, "CARD");
        PaymentResponse payment = paymentHelper.createPayment(
                created.getId(), "CARD", IdempotencyKey.random()
        );
        orderDb.awaitStatus(created.getId(), "PREPARING", LIFECYCLE_TIMEOUT);
        orderDb.awaitStatus(created.getId(), "DONE", LIFECYCLE_TIMEOUT);
        assertThat(ingredientDb.findById(ingredient.getId()).orElseThrow().getOnHand())
                .isEqualTo(startStock - qtyNeededPerPortion);
    }
}
```

SOAP: поставщик мяса

```
@Test
void restockAccepted(IntegrationStubs stubs, KafkaTestBus kafka) {
    int ingredientId = ingredientHelper.createSoapIngredientId("ACCEPTED", "MEAT_BEEF");
    owner.ingredients().restock(ingredientId, 100).assertStatus(200);
    assertThat(stubs.meat().recordedEnvelopesForIngredient(ingredientId))
            .singleElement()
            .satisfies(request -> assertThat(request)
                    .contains("<code>MEAT_BEEF</code>")
                    .contains("<quality>STANDARD</quality>"));
    StockEvent event = kafka.stock().waitRestockPlaced(ingredientId);
    assertThat(event.getStatus()).isIn("ACCEPTED", "WAITLIST");
}
```

UI: оплата заказа

```
@Test
void payPendingOrderViaUi(IntegrationStubs stubs) {
    stubs.payments().responseSuccess(txnId);
    ordersPage.openOrders()
            .cardById(created.getId())
            .pay();
    ordersPage.expectToast("PAYMENT OK");
    orderDb.awaitStatus(created.getId(), "PAID", PAYMENT_TIMEOUT);
}
```

## Локальные dev-креды

Все пароли в docker-compose.yml и shawarma.properties — локальные dev-значения для контейнеров на localhost. Не являются секретами, не используются нигде, кроме этой машины.

## CI

GitHub Actions запускает:

1. api-tests — API/E2E-тесты после docker compose up -d и healthcheck
2. ui-tests — UI-тесты под xvfb-run (запускаются после зелёных API-тестов)
3. publish-allure — объединённый Allure-отчёт публикуется на GitHub Pages

Ссылка на отчёт: https://MaksimSvyat.github.io/shawarma-aqa-showcase/