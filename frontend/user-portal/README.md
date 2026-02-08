# Nodeorb Logistics Platform - User Portal

## Overview
Пользовательский портал для логистической платформы Nodeorb. Предоставляет доступ к различным модулям платформы и управление операциями.

## Structure

### Core Files
- `index.html` - Главная страница
- `package.json` - Конфигурация проекта
- `vite.config.ts` - Конфигурация Vite
- `tailwind.config.js` - Конфигурация Tailwind CSS
- `tsconfig.json` - Конфигурация TypeScript

### Source Code
- `src/` - Исходный код приложения
- `src/App.tsx` - Главный компонент приложения
- `src/main.tsx` - Точка входа
- `src/index.css` - Глобальные стили
- `src/components/` - React компоненты
- `src/modules/` - Основные модули приложения

### Components
- `Layout.tsx` - Общий макет
- `Offline.tsx` - Компонент для работы офлайн
- `Sidebar.tsx` - боковая панель
- `StepUpAuthModal.tsx` - Модальное окно для усиленной аутентификации
- `TopBar.tsx` - верхняя панель

### Modules
- `Analytics.tsx` - Аналитика
- `CRM.tsx` - CRM (Customer Relationship Management)
- `ERP.tsx` - ERP (Enterprise Resource Planning)
- `FMS.tsx` - Управление флотом
- `Inventory.tsx` - Управление запасами
- `Marketplace.tsx` - Фрахтовый маркетплейс
- `SecurityHub.tsx` - Безопасность
- `TMS.tsx` - Управление транспортом
- `WMS.tsx` - Управление складом

### Configuration & Store
- `src/config/` - Конфигурационные файлы
- `src/core/` - Основная логика приложения
- `src/providers/` - Провайдеры состояния
- `src/store/` - Хранилище состояния
- `src/types/` - Типы TypeScript

## Functionality

### Dashboard
- Обзор всех ключевых метрик
- Быстрый доступ к часто используемым модулям
- Уведомления и алерты

### Analytics
- Отчеты и аналитика по операциям
- Графики и диаграммы
- Экспорт данных

### CRM
- Управление клиентами
- История взаимодействий
- Коммуникация с клиентами

### ERP
- Управление ресурсами предприятия
- Финансовая аналитика
- Интеграция с учетными системами

### FMS (Fleet Management)
- Управление транспортным флотом
- Трекинг в реальном времени
- Техническое обслуживание

### Inventory Management
- Управление запасами
- Инвентаризация
- Отслеживание местоположения товаров

### Marketplace
- Создание и управление заказами
- Просмотр ставок
- Автоматическое сопоставление

### Security Hub
- Управление безопасностью
- Проверка соответствия
- Уведомления о безопасности

### TMS (Transportation Management)
- Планирование маршрутов
- Расчет стоимости
- Отслеживание доставки

### WMS (Warehouse Management)
- Управление складскими операциями
- Приемка и отгрузка
- Обратная логистика

## Technology Stack
- **React** 18 + TypeScript
- **Vite** - сборщик
- **Tailwind CSS** - стилизация
- **Node.js** - окружение
- **Redux** - управление состоянием
- **WebSocket** - реальное время

## Configuration

### Package.json
```json
{
  "name": "nodeorb-user-portal",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "lint": "eslint . --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
    "preview": "vite preview"
  },
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.66",
    "@types/react-dom": "^18.2.22",
    "@typescript-eslint/eslint-plugin": "^7.2.0",
    "@typescript-eslint/parser": "^7.2.0",
    "@vitejs/plugin-react": "^4.2.1",
    "autoprefixer": "^10.4.19",
    "eslint": "^8.57.0",
    "eslint-plugin-react-hooks": "^4.6.0",
    "eslint-plugin-react-refresh": "^0.4.6",
    "postcss": "^8.4.38",
    "tailwindcss": "^3.4.1",
    "typescript": "^5.2.2",
    "vite": "^5.2.0"
  }
}
```

## Running the Service

### Local Development
```bash
cd c:\Project\Nodeorb\frontend\user-portal
npm install
npm run dev
```

### Build for Production
```bash
cd c:\Project\Nodeorb\frontend\user-portal
npm run build
```

### Preview Production Build
```bash
cd c:\Project\Nodeorb\frontend\user-portal
npm run preview
```

## Deployment

### Docker
```bash
cd c:\Project\Nodeorb
docker build -t nodeorb-user-portal:latest -f frontend/user-portal/Dockerfile .
docker run -d -p 3001:3001 --network nodeorb-network nodeorb-user-portal:latest
```

### Static Hosting
Собранные файлы находятся в директории `dist/` и могут быть развернуты на любом статическом хостинге (Netlify, Vercel, GitHub Pages).

## Contributing
Please follow the project guidelines for contributing. Create a feature branch, make your changes, and submit a pull request.

## License
MIT License - see LICENSE file for details.