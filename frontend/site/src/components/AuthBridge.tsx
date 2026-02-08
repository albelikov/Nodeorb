import React, { useEffect, useState } from 'react'

interface AuthBridgeProps {
  children: React.ReactNode
}

const AuthBridge: React.FC<AuthBridgeProps> = ({ children }) => {
  const [isCheckingAuth, setIsCheckingAuth] = useState(true)

  useEffect(() => {
    const checkAuth = async () => {
      // 1. Отримуємо параметри з URL (Keycloak надсилає ?code=... після логіну)
      const urlParams = new URLSearchParams(window.location.search);
      const code = urlParams.get('code');
      const token = localStorage.getItem('auth_token');

      // 2. Якщо ми щойно повернулися з Keycloak з кодом
      if (code) {
        console.log('Отримано код від Keycloak, обробка...');
        // Тут зазвичай іде запит на бекенд для обміну коду на токен
        // Поки що просто імітуємо або зберігаємо (для тесту)
        setIsCheckingAuth(false);
        return;
      }

      // 3. Якщо токен вже є, перевіряємо його
      if (token) {
        try {
          // Тимчасово закоментуйте fetch, якщо у вас ще не запущено бекенд /api/auth/verify
          // щоб не вилітати в catch
          /*
          const response = await fetch('http://localhost:8080/api/auth/verify', {...});
          if (response.ok) {
            window.location.href = 'http://localhost:5173'; // Порт User Portal
            return;
          }
          */
          setIsCheckingAuth(false);
          return;
        } catch (error) {
          console.error('Помилка верифікації:', error);
        }
      }

      // 4. Якщо токена немає і ми НЕ в процесі логіну — зупиняємо перевірку
      // Даємо користувачу побачити лендинг, а редирект зробимо ПО КЛІКУ на кнопку
      setIsCheckingAuth(false);
    }

    checkAuth()
  }, [])

  if (isCheckingAuth) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-900">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-400"></div>
      </div>
    )
  }

  return <>{children}</>
}

export default AuthBridge