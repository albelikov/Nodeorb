# Сервис безопасности и комплаенс (Security & Compliance Management Service - SCM)

## Описание
Централизованный сервис управления безопасностью, соответствием требованиям и аудитом.

## Функциональность
- Управление доступом (IAM - Identity & Access Management)
- Защита данных и конфиденциальность
- Обеспечение соответствия стандартам
- Мониторинг угроз в реальном времени
- Корреляция событий безопасности
- Ответ на инциденты
- Управление рисками

## Технологии
- **Язык**: Kotlin
- **Фреймворк**: Spring Boot
- **СУБД**: PostgreSQL для аудита
- **Шифрование**: FIPS 140-2
- **Мониторинг**: ELK Stack
- **Аутентификация**: OAuth2 + OpenID Connect

## Конфигурация
```properties
server.port=8087
security.audit.database=audit_logs
oauth2.provider=keycloak
logging.level=INFO
```

## Запуск
```bash
./gradlew bootRun
```

## Компоненты
### IAM Module
- Управление пользователями и группами
- Многофакторная аутентификация
- Единый вход (SSO)
- Управление сессиями

### Data Protection Module
- Шифрование данных
- Маскирование PII
- Контроль доступа на основе атрибутов (ABAC)
- Следование принципу least privilege

### Compliance Module
- Автоматическая проверка соответствия
- Генерация отчетов
- Управление политиками
- Tracking compliance gaps

### Audit Module
- Логирование всех действий
- Immutable audit trail
- Поддержка внешних аудитов
- Chain of custody tracking

## Безопасность
- Zero Trust Architecture
- mTLS для сервис-сервис коммуникации
- Advanced threat detection
- Automated incident response
- Regular penetration testing

## Соответствие требованиям
### US Standards
- FedRAMP Moderate/High
- CMMC Level 3
- NIST 800-171
- DFARS 252.204-7012
- ITAR/EAR compliance

### EU Standards
- GDPR (General Data Protection Regulation)
- CSRD (Corporate Sustainability Reporting)
- Digital Transport Law
- Ecodesign Directive

### Двойное назначение
- Безопасные режимы переключения
- Government emergency operations
- Military supply chain protection
- Critical infrastructure resilience

## Интеграция с SOC
- Real-time alerting to Security Operations Center
- Integration with CISA AIS for threat intelligence
- Automated playbooks for incident response
- Compliance dashboards for management

## Данные и Резиденство
- Поддержка требований к резидентству данных
- Динамические геозаборы для маршрутизации
- Межграничная передача в соответствии с законами
- Crypto-agile encryption algorithms