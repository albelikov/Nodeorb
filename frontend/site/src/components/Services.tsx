import React from 'react'
import { motion } from 'framer-motion'

const services = [
  {
    title: 'Marketplace',
    description: 'Платформа для торгівлі вантажами з автоматичними торгами та біометричною автентифікацією',
    icon: '📦',
    features: ['Аукціони', 'Біометричний захист', 'WORM логи', 'Реальний час']
  },
  {
    title: 'WMS',
    description: 'Система управління складами з інтеграцією IoT та автоматизацією процесів',
    icon: '🏭',
    features: ['RFID', 'AGV роботи', 'Мобільні додатки', 'Інвентаризація']
  },
  {
    title: 'TMS',
    description: 'Транспортна система з оптимізацією маршрутів та розрахунком вуглецевого сліду',
    icon: '🚚',
    features: ['Маршрутизація', 'Геозони', 'Вуглецевий слід', 'GIS інтеграція']
  },
  {
    title: 'FMS',
    description: 'Флот-менеджмент з відстеженням транспорту та диспетчеризацією',
    icon: '🚛',
    features: ['GPS трекінг', 'Диспетчер', 'Мобільні додатки', 'Аналітика']
  },
  {
    title: 'ERP',
    description: 'Комплексне управління бізнес-процесами з фінансами та персоналом',
    icon: '💼',
    features: ['Фінанси', 'Персонал', 'Звітність', 'Інтеграція']
  },
  {
    title: 'CRM',
    description: 'Система управління відносинами з клієнтами та B2B партнерами',
    icon: '👥',
    features: ['Клієнти', 'Партнери', 'Аналітика', 'Автоматизація']
  },
  {
    title: 'Analytics',
    description: 'Аналітична платформа з AI для прогнозування та оптимізації',
    icon: '📊',
    features: ['AI аналітика', 'Прогнозування', 'KPI', 'Візуалізація']
  },
  {
    title: 'Inventory',
    description: 'Система управління запасами з контролем та аналітикою',
    icon: '📦',
    features: ['Контроль', 'Аналітика', 'Оптимізація', 'Інтеграція']
  },
  {
    title: 'Fleet',
    description: 'Управління автопарком з моніторингом та обслуговуванням',
    icon: '🚗',
    features: ['Моніторинг', 'Обслуговування', 'Витрати', 'Аналітика']
  },
  {
    title: 'Documents',
    description: 'Електронний документообіг з цифровим підписом та архівом',
    icon: '📄',
    features: ['Е-документи', 'Підписи', 'Архів', 'Шаблони']
  },
  {
    title: 'Support',
    description: 'Система підтримки з тікетами та базою знань',
    icon: '🛠️',
    features: ['Тікети', 'База знань', 'Чат', 'Статистика']
  }
]

const Services: React.FC = () => {
  return (
    <section className="py-20 bg-gradient-to-b from-transparent to-dark-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          className="text-center mb-16"
        >
          <h2 className="text-3xl md:text-5xl font-bold text-white mb-4">
            Комплексні рішення для{' '}
            <span className="bg-gradient-to-r from-neon-blue to-neon-green bg-clip-text text-transparent">
              логістики
            </span>
          </h2>
          <p className="text-lg text-gray-300 max-w-3xl mx-auto">
            11 спеціалізованих модулів, що працюють як єдина екосистема. 
            Кожен модуль можна використовувати окремо або в комплексі.
          </p>
        </motion.div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {services.map((service, index) => (
            <motion.div
              key={service.title}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              className="group relative"
            >
              <div className="relative bg-gradient-to-br from-dark-700 to-dark-800 p-6 rounded-xl border border-gray-700 hover:border-neon-blue transition-all duration-300 hover:shadow-xl hover:shadow-neon-blue/20">
                {/* Floating Icon */}
                <div className="absolute -top-4 -right-4 text-6xl opacity-20 group-hover:opacity-50 transition-opacity duration-300">
                  {service.icon}
                </div>
                
                {/* Content */}
                <div className="relative z-10">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-xl font-bold text-white">{service.title}</h3>
                    <div className="w-8 h-8 bg-neon-blue/20 rounded-full flex items-center justify-center">
                      <div className="w-3 h-3 bg-neon-blue rounded-full animate-pulse"></div>
                    </div>
                  </div>
                  
                  <p className="text-gray-300 text-sm mb-4 leading-relaxed">
                    {service.description}
                  </p>
                  
                  <div className="grid grid-cols-2 gap-2">
                    {service.features.map((feature, idx) => (
                      <div
                        key={idx}
                        className="flex items-center space-x-2 text-xs text-gray-400 bg-dark-600 px-3 py-1 rounded-full border border-gray-600"
                      >
                        <div className="w-2 h-2 bg-neon-green rounded-full"></div>
                        <span>{feature}</span>
                      </div>
                    ))}
                  </div>
                </div>
                
                {/* Hover Overlay */}
                <div className="absolute inset-0 bg-gradient-to-br from-neon-blue/5 to-neon-green/5 rounded-xl opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
              </div>
            </motion.div>
          ))}
        </div>

        {/* Stats Section */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.5 }}
          className="mt-20 grid grid-cols-1 md:grid-cols-4 gap-8 text-center"
        >
          {[
            { label: 'Активні користувачі', value: '10,000+', color: 'neon-blue' },
            { label: 'Операції на день', value: '50,000+', color: 'neon-green' },
            { label: 'Підключені компанії', value: '500+', color: 'neon-purple' },
            { label: 'Глобальна підтримка', value: '24/7', color: 'neon-red' }
          ].map((stat, index) => (
            <div key={index} className="bg-dark-700 p-6 rounded-xl border border-gray-700">
              <div className={`text-2xl font-bold text-${stat.color} mb-2`}>{stat.value}</div>
              <div className="text-gray-400 text-sm">{stat.label}</div>
            </div>
          ))}
        </motion.div>
      </div>
    </section>
  )
}

export default Services