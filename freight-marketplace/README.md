# Грузовая биржа (Freight Marketplace)

## Описание
Платформа для автоматического сопоставления грузов и транспортных средств с использованием blockchain.

## Функциональность
- Публикация грузов и транспорта
- Интеллектуальный поиск и сопоставление
- Аукционное ценообразование
- Рейтинговая система перевозчиков
- Управление договорами через смарт-контракты
- Эскроу-счета для безопасных расчетов
- Электронные подписи документов

## Технологии
- **Язык**: Kotlin
- **Blockchain**: Hyperledger Fabric
- **Смарт-контракты**: Chaincode
- **Sharding**: Для высокой нагрузки
- **СУБД**: PostgreSQL + Redis
- **КЭШ**: Redis

## Конфигурация
```properties
server.port=8085
blockchain.network=fabric://localhost:7054
escrow.bank.integration=SWIFT/BIC
```

## Запуск
```bash
./gradlew bootRun
```

## Архитектура Blockchain
- Private consortium network
- Immutable transaction ledger
- Role-based access control
- Auditable by regulatory bodies

## Безопасность
- Zero-knowledge proofs
- Multi-signature contracts
- Regulatory compliance auditing
- GDPR/CCPA data protection

## Интеграция
- Банковские системы (SWIFT, IBAN)
- Правительственные порталы
- Существующие логистические платформы

## Согласование
Соответствует:
- Financial industry standards
- Anti-money laundering (AML)
- Know your customer (KYC)
- Digital freight exchange regulations