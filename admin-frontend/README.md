# Клиентская часть административной панели (Admin Frontend)

## Overview
Фронтенд приложение для администрирования логистической платформы Nodeorb. Поддерживает веб, десктоп и мобильные устройства с общим кодом на Kotlin Multiplatform.

## Structure

### Core Modules
- `src/commonMain` - Общий код для всех платформ
- `src/jsMain` - Веб-версия (React)
- `src/desktopMain` - Десктопная версия
- `src/androidMain` - Android-версия
- `src/iosMain` - iOS-версия

### Key Components

#### Common Logic
- `model/` - Данные и модели приложения
- `service/` - Сервисы для работы с API
- `ui/` - Общие компоненты интерфейса
- `navigation/` - Навигация между экранами

#### Web Version (React)
- `components/` - React компоненты
- `pages/` - Страницы приложения
- `hooks/` - Кастомные хуки
- `utils/` - Утилиты
- `styles/` - Глобальные стили

## Functionality

### Dashboard & Monitoring
- Панель мониторинга операций в реальном времени
- Графики и диаграммы производительности
- Уведомления о проблемах и алерты
- Дашборд с ключевыми метриками

### User Management
- Таблица пользователей с фильтрацией и поиском
- Формы для создания и редактирования пользователей
- Управление ролями и правами доступа
- История действий пользователей

### Analytics & Reporting
- Отчеты по использованию системы
- Статистика операций и пользователей
- Графики и диаграммы аналитики
- Экспорт отчетов в PDF/Excel

### System Configuration
- Настройки безопасности и доступа
- Конфигурация интеграций с внешними системами
- Параметры работы платформы
- Управление ключами и сертификатами

## Technology Stack
- **Кроссплатформенность**: Kotlin Multiplatform 1.9.23
- **Веб**: React 18 + TypeScript + Tailwind CSS
- **Нативные приложения**: Android (Kotlin), iOS (Swift)
- **Реальное время**: WebSocket
- **Аутентификация**: OAuth2 + JWT
- **API клиент**: Ktor
- **Состояние приложения**: Redux (React), KMM ViewModel

## Configuration

### Web Version
```json
{
  "api": {
    "baseUrl": "http://localhost:8081/api",
    "timeout": 30000
  },
  "websocket": {
    "url": "ws://localhost:8081/ws"
  },
  "auth": {
    "clientId": "admin-frontend",
    "redirectUri": "http://localhost:3000/callback"
  }
}
```

## Running the Service

### Local Development

#### Web Version (React)
```bash
cd c:\Project\Nodeorb\admin-frontend
cd src/jsMain
npm install
npm start
```

#### Desktop Version
```bash
cd c:\Project\Nodeorb
./gradlew :admin-frontend:run
```

#### Build for All Platforms
```bash
cd c:\Project\Nodeorb
./gradlew :admin-frontend:build
```

### Docker Deployment
```bash
cd c:\Project\Nodeorb
docker build -t admin-frontend:latest -f admin-frontend/Dockerfile .
docker run -d -p 3000:3000 --network nodeorb-network admin-frontend:latest
```

## API Integration

### REST API
- Подключение к Admin Backend
- Аутентификация через OAuth2
- Запросы к endpoints для управления пользователями
- Запросы к endpoints для мониторинга

### WebSocket
- Подключение к серверу для получения实时 updates
- Обработка событий и алертов
- Обновление данных на дашборде в реальном времени

## Contributing
Please follow the project guidelines for contributing. Create a feature branch, make your changes, and submit a pull request.

## License
MIT License - see LICENSE file for details.