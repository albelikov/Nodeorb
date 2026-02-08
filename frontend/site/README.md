# Nodeorb Logistics Platform - Static Site

## Overview
Статический сайт для логистической платформы Nodeorb. Представляет информацию о платформе, ее услугах и возможностях.

## Structure

### Core Files
- `index.html` - Главная страница
- `package.json` - Конфигурация проекта
- `vite.config.ts` - Конфигурация Vite
- `tailwind.config.js` - Конфигурация Tailwind CSS

### Source Code
- `src/` - Исходный код приложения
- `src/App.tsx` - Главный компонент приложения
- `src/main.tsx` - Точка входа
- `src/index.css` - Глобальные стили
- `src/components/` - React компоненты

### Components
- `AuthBridge.tsx` - Компонент для аутентификации
- `Hero.tsx` - Герой секция
- `LandingPage.tsx` - Лендинговая страница
- `Layout.tsx` - Общий макет
- `SCMProtection.tsx` - Информация о защите SCM
- `Services.tsx` - Сервисы платформы

## Functionality

### Landing Page
- Представление платформы и ее ценностей
- Информация о ключевых услугах
- Секция с преимуществами
- Форма для обратной связи

### Authentication
- Интеграция с Auth Bridge для аутентификации
- Поддержка различных методов входа

### Services Display
- Просмотр доступных сервисов
- Детальная информация о каждом сервисе
- Сравнение функций

### SCM Protection
- Информация о защите данных
- Безопасность и соответствие требованиям
- Примеры использования

## Technology Stack
- **React** 18 + TypeScript
- **Vite** - сборщик
- **Tailwind CSS** - стилизация
- **Node.js** - окружение

## Configuration

### Package.json
```json
{
  "name": "nodeorb-site",
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
cd c:\Project\Nodeorb\frontend\site
npm install
npm run dev
```

### Build for Production
```bash
cd c:\Project\Nodeorb\frontend\site
npm run build
```

### Preview Production Build
```bash
cd c:\Project\Nodeorb\frontend\site
npm run preview
```

## Deployment

### Docker
```bash
cd c:\Project\Nodeorb
docker build -t nodeorb-site:latest -f frontend/site/Dockerfile .
docker run -d -p 3000:3000 --network nodeorb-network nodeorb-site:latest
```

### Static Hosting
Собранные файлы находятся в директории `dist/` и могут быть развернуты на любом статическом хостинге (Netlify, Vercel, GitHub Pages).

## Contributing
Please follow the project guidelines for contributing. Create a feature branch, make your changes, and submit a pull request.

## License
MIT License - see LICENSE file for details.