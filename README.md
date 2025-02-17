# Balance Management Service

## Описание проекта
Сервис управления банковскими счетами с возможностью выполнения базовых финансовых операций с точностью до копеек.


## Архитектура проекта
- Язык: Java 21
- Фреймворк: Spring Boot 3.2.2
- База данных: PostgreSQL
- Миграции: Flyway
- Документация API: Swagger

## Функциональность
- Пополнение счета
- Списание средств
- Перевод между счетами
- Получение баланса
- Просмотр выписки по счету

## Ограничения проекта
- Моновалютность
- Операции с точностью до копеек

## API Эндпоинты

- POST `/api/v1/accounts/{id}/deposit` — Пополнение счета
- POST `/api/v1/accounts/{id}/withdrew` — Списание средств
- POST `/api/v1/accounts/{formId}/transfer/{toId}` — Перевод между счетами
- GET `/api/v1/accounts/{id}/balance` — Получить баланс счета
- GET `/api/v1/accounts/{id}/statement` — Получить выписку по счету

## Формат идентификаторов счетов
Счета имеют UUID формата:
`123e4567-e89b-12d3-a456-426614174000`

## Перед началом работы нужно:
- Настроить подключение к внешней бд PostgreSQL
- Поменять настройки в `application.properties` на spring.jpa.hibernate.ddl-auto=update
(только для первоначальной настройки), после первого запуска рекомендую вернуть spring.jpa.hibernate.ddl-auto=validate
- Чтобы проверить работу эндпоинтов, нужно добавить пару счетов:
  INSERT INTO accounts (id, balance, created_at)
  VALUES
  ('550e8400-e29b-41d4-a716-446655440000', 1000.00, NOW()),
  ('123e4567-e89b-12d3-a456-426614174000', 500.00, NOW());

## Документация API 
После запуска приложения документация доступна по адресу Swagger UI: http://localhost:8080/swagger-ui.html