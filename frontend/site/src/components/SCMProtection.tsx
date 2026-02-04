import React from 'react'
import { motion } from 'framer-motion'

const SCMProtection: React.FC = () => {
  return (
    <section className="py-20 bg-gradient-to-b from-dark-800 to-dark-900">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          className="text-center mb-16"
        >
          <h2 className="text-3xl md:text-5xl font-bold text-white mb-4">
            <span className="bg-gradient-to-r from-neon-blue to-neon-green bg-clip-text text-transparent">
              Безпека
            </span>{' '}
            та{' '}
            <span className="bg-gradient-to-r from-neon-purple to-neon-red bg-clip-text text-transparent">
              Надійність
            </span>
          </h2>
          <p className="text-lg text-gray-300 max-w-4xl mx-auto">
            Наша система забезпечує максимальний захист ваших даних та операцій 
            за допомогою сучасних технологій біометричної автентифікації та незмінних логів.
          </p>
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          {/* Left Content */}
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.8 }}
          >
            <div className="space-y-8">
              {/* Biometric Protection */}
              <div className="relative">
                <div className="absolute -left-4 top-0 w-2 h-2 bg-neon-green rounded-full animate-pulse"></div>
                <h3 className="text-2xl font-bold text-white mb-4 flex items-center">
                  <span className="w-8 h-8 bg-neon-green/20 rounded-full flex items-center justify-center mr-4">
                    <div className="w-3 h-3 bg-neon-green rounded-full"></div>
                  </span>
                  Біометричний захист
                </h3>
                <p className="text-gray-300 leading-relaxed">
                  Кожна операція в системі підтверджується біометричними даними користувача. 
                  Відбитки пальців, розпізнавання обличчя та інші біометричні методи забезпечують 
                  100% ідентифікацію та захист від несанкціонованого доступу.
                </p>
                <div className="mt-4 grid grid-cols-2 gap-3">
                  {['Відбитки пальців', 'Розпізнавання обличчя', 'Ірис очей', 'Голосовий аналіз'].map((feature, idx) => (
                    <div key={idx} className="flex items-center space-x-2 text-sm text-gray-400 bg-dark-600 px-3 py-2 rounded-lg border border-gray-600">
                      <div className="w-2 h-2 bg-neon-green rounded-full"></div>
                      <span>{feature}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* WORM Technology */}
              <div className="relative">
                <div className="absolute -left-4 top-0 w-2 h-2 bg-neon-blue rounded-full animate-pulse"></div>
                <h3 className="text-2xl font-bold text-white mb-4 flex items-center">
                  <span className="w-8 h-8 bg-neon-blue/20 rounded-full flex items-center justify-center mr-4">
                    <div className="w-3 h-3 bg-neon-blue rounded-full"></div>
                  </span>
                  WORM технологія
                </h3>
                <p className="text-gray-300 leading-relaxed">
                  Всі логи та транзакції записуються в незмінні WORM (Write Once, Read Many) сховища. 
                  Це забезпечує повну прозорість та неможливість підробки даних. 
                  Кожна операція має цифровий відбиток часу та біометричну підписку.
                </p>
                <div className="mt-4 grid grid-cols-2 gap-3">
                  {['Незмінність', 'Цифровий підпис', 'Часові мітки', 'Аудит-логи'].map((feature, idx) => (
                    <div key={idx} className="flex items-center space-x-2 text-sm text-gray-400 bg-dark-600 px-3 py-2 rounded-lg border border-gray-600">
                      <div className="w-2 h-2 bg-neon-blue rounded-full"></div>
                      <span>{feature}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Security Features */}
              <div className="relative">
                <div className="absolute -left-4 top-0 w-2 h-2 bg-neon-purple rounded-full animate-pulse"></div>
                <h3 className="text-2xl font-bold text-white mb-4 flex items-center">
                  <span className="w-8 h-8 bg-neon-purple/20 rounded-full flex items-center justify-center mr-4">
                    <div className="w-3 h-3 bg-neon-purple rounded-full"></div>
                  </span>
                  Безпечність
                </h3>
                <p className="text-gray-300 leading-relaxed">
                  Система використовує найсучасніші методи шифрування, мультифакторну автентифікацію 
                  та розподілену архітектуру. Всі дані захищені відповідно до міжнародних стандартів 
                  безпеки та вимог регуляторів.
                </p>
                <div className="mt-4 grid grid-cols-2 gap-3">
                  {['Шифрування AES-256', 'Мультифакторна автентифікація', 'Розподілена архітектура', 'Відповідність стандартам'].map((feature, idx) => (
                    <div key={idx} className="flex items-center space-x-2 text-sm text-gray-400 bg-dark-600 px-3 py-2 rounded-lg border border-gray-600">
                      <div className="w-2 h-2 bg-neon-purple rounded-full"></div>
                      <span>{feature}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </motion.div>

          {/* Right Visual */}
          <motion.div
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.8, delay: 0.2 }}
            className="relative"
          >
            <div className="relative bg-gradient-to-br from-dark-700 to-dark-800 p-8 rounded-2xl border border-gray-700 shadow-2xl">
              {/* Security Dashboard */}
              <div className="grid grid-cols-2 gap-6">
                {/* Security Status */}
                <div className="bg-dark-600 p-6 rounded-lg border border-gray-600">
                  <div className="flex items-center justify-between mb-4">
                    <h4 className="font-semibold text-white">Статус безпеки</h4>
                    <div className="w-3 h-3 bg-neon-green rounded-full animate-pulse"></div>
                  </div>
                  <div className="space-y-3">
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-400">Біометрична автентифікація</span>
                      <span className="text-neon-green">Активна</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-400">WORM логи</span>
                      <span className="text-neon-blue">Захищені</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-400">Шифрування</span>
                      <span className="text-neon-purple">AES-256</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-400">Аудит</span>
                      <span className="text-neon-red">24/7</span>
                    </div>
                  </div>
                </div>

                {/* Threat Monitor */}
                <div className="bg-dark-600 p-6 rounded-lg border border-gray-600">
                  <div className="flex items-center justify-between mb-4">
                    <h4 className="font-semibold text-white">Моніторинг загроз</h4>
                    <div className="w-3 h-3 bg-neon-red rounded-full animate-pulse"></div>
                  </div>
                  <div className="space-y-3">
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-400">Активні загрози</span>
                      <span className="text-neon-green">0</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-400">Блоковано</span>
                      <span className="text-neon-blue">1,234</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-400">Аналіз</span>
                      <span className="text-neon-purple">AI</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-400">Відповідь</span>
                      <span className="text-neon-red">Миттєва</span>
                    </div>
                  </div>
                </div>

                {/* Compliance */}
                <div className="bg-dark-600 p-6 rounded-lg border border-gray-600 col-span-2">
                  <div className="flex items-center justify-between mb-4">
                    <h4 className="font-semibold text-white">Відповідність стандартам</h4>
                    <div className="flex space-x-2">
                      <div className="w-3 h-3 bg-neon-green rounded-full"></div>
                      <div className="w-3 h-3 bg-neon-blue rounded-full"></div>
                      <div className="w-3 h-3 bg-neon-purple rounded-full"></div>
                    </div>
                  </div>
                  <div className="grid grid-cols-4 gap-2 text-xs text-gray-400">
                    <div className="text-center">GDPR</div>
                    <div className="text-center">ISO 27001</div>
                    <div className="text-center">SOC 2</div>
                    <div className="text-center">PCI DSS</div>
                  </div>
                </div>
              </div>

              {/* Floating Elements */}
              <div className="absolute -top-4 -right-4 w-24 h-24 bg-neon-green/20 rounded-full blur-xl"></div>
              <div className="absolute -bottom-4 -left-4 w-24 h-24 bg-neon-blue/20 rounded-full blur-xl"></div>
              <div className="absolute top-1/2 -right-8 w-16 h-16 bg-neon-purple/20 rounded-full blur-lg animate-pulse"></div>
            </div>
          </motion.div>
        </div>

        {/* Security Badges */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.4 }}
          className="mt-16 grid grid-cols-2 md:grid-cols-4 gap-6 text-center"
        >
          {[
            { label: 'Біометрична автентифікація', value: '100%', color: 'neon-green' },
            { label: 'Незмінні логи', value: 'WORM', color: 'neon-blue' },
            { label: 'Шифрування', value: 'AES-256', color: 'neon-purple' },
            { label: 'Моніторинг', value: '24/7', color: 'neon-red' }
          ].map((badge, index) => (
            <div key={index} className="bg-dark-700 p-6 rounded-xl border border-gray-700 hover:border-neon-blue transition-all duration-300">
              <div className={`text-2xl font-bold text-${badge.color} mb-2`}>{badge.value}</div>
              <div className="text-gray-400 text-sm">{badge.label}</div>
            </div>
          ))}
        </motion.div>
      </div>
    </section>
  )
}

export default SCMProtection