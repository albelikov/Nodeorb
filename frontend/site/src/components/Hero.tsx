import React from 'react'
import { motion } from 'framer-motion'

const Hero: React.FC = () => {
  return (
    <section className="relative py-20 md:py-32 overflow-hidden">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          
          {/* Left Content */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            className="text-center lg:text-left"
          >
            <div className="mb-6">
              <span className="inline-flex items-center px-4 py-2 rounded-full text-sm font-medium bg-neon-blue/20 text-neon-blue border border-neon-blue/30">
                <div className="w-2 h-2 bg-neon-blue rounded-full mr-2 animate-pulse"></div>
                Защита нового покоління
              </span>
            </div>
            
            <h1 className="text-4xl md:text-6xl lg:text-7xl font-bold text-white mb-6 leading-tight">
              Nodeorb: Екосистема{' '}
              <span className="bg-gradient-to-r from-neon-blue to-neon-green bg-clip-text text-transparent">
                захищеної
              </span>{' '}
              логістики
            </h1>
            
            <p className="text-lg md:text-xl text-gray-300 mb-8 leading-relaxed">
              Сучасна платформа для управління ланцюгами поставок з біометричним захистом, 
              незмінними логами та розумною автоматизацією. Безпека, надійність та контроль 
              на кожному етапі вашого бізнесу.
            </p>
            
            <div className="flex flex-col sm:flex-row gap-4 justify-center lg:justify-start">
              <motion.button
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                className="group relative px-8 py-4 bg-gradient-to-r from-neon-blue to-neon-green text-dark-900 font-semibold rounded-lg overflow-hidden shadow-lg shadow-neon-blue/25 hover:shadow-neon-blue/40 transition-all duration-300"
              >
                <span className="relative z-10">Демонстрація</span>
                <div className="absolute inset-0 bg-white opacity-0 group-hover:opacity-10 transition-opacity duration-300"></div>
              </motion.button>
              
              <motion.button
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                className="px-8 py-4 border-2 border-gray-600 text-gray-300 font-semibold rounded-lg hover:border-neon-blue hover:text-neon-blue transition-all duration-300"
              >
                Дізнатись більше
              </motion.button>
            </div>
            
            <div className="mt-8 flex items-center justify-center lg:justify-start space-x-6 text-sm text-gray-400">
              <div className="flex items-center space-x-2">
                <div className="w-3 h-3 bg-neon-green rounded-full animate-pulse"></div>
                <span>Активні користувачі</span>
              </div>
              <div className="flex items-center space-x-2">
                <div className="w-3 h-3 bg-neon-blue rounded-full animate-pulse"></div>
                <span>Безпечні операції</span>
              </div>
              <div className="flex items-center space-x-2">
                <div className="w-3 h-3 bg-neon-purple rounded-full animate-pulse"></div>
                <span>Реальний час</span>
              </div>
            </div>
          </motion.div>
          
          {/* Right Visual */}
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.8, delay: 0.2 }}
            className="relative"
          >
            <div className="relative">
              {/* Main Visual */}
              <div className="relative bg-gradient-to-br from-dark-700 to-dark-800 p-8 rounded-2xl border border-gray-700 shadow-2xl">
                <div className="grid grid-cols-2 gap-4">
                  {/* Dashboard Cards */}
                  <div className="bg-dark-600 p-4 rounded-lg border border-gray-600">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-xs text-gray-400">Завантаження</span>
                      <span className="text-xs text-neon-green">Активно</span>
                    </div>
                    <div className="text-2xl font-bold text-white">1,234</div>
                    <div className="w-full bg-gray-600 rounded-full h-2 mt-2">
                      <div className="bg-neon-blue h-2 rounded-full w-3/4"></div>
                    </div>
                  </div>
                  
                  <div className="bg-dark-600 p-4 rounded-lg border border-gray-600">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-xs text-gray-400">Маршрути</span>
                      <span className="text-xs text-neon-green">Оптимізовано</span>
                    </div>
                    <div className="text-2xl font-bold text-white">98%</div>
                    <div className="w-full bg-gray-600 rounded-full h-2 mt-2">
                      <div className="bg-neon-green h-2 rounded-full w-5/6"></div>
                    </div>
                  </div>
                  
                  <div className="bg-dark-600 p-4 rounded-lg border border-gray-600 col-span-2">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-xs text-gray-400">Безпека</span>
                      <span className="text-xs text-neon-blue">Максимум</span>
                    </div>
                    <div className="grid grid-cols-4 gap-2">
                      <div className="text-center">
                        <div className="w-3 h-3 bg-neon-green rounded-full mx-auto mb-1"></div>
                        <span className="text-xs text-gray-400">WORM</span>
                      </div>
                      <div className="text-center">
                        <div className="w-3 h-3 bg-neon-blue rounded-full mx-auto mb-1"></div>
                        <span className="text-xs text-gray-400">Biometric</span>
                      </div>
                      <div className="text-center">
                        <div className="w-3 h-3 bg-neon-purple rounded-full mx-auto mb-1"></div>
                        <span className="text-xs text-gray-400">Audit</span>
                      </div>
                      <div className="text-center">
                        <div className="w-3 h-3 bg-neon-red rounded-full mx-auto mb-1"></div>
                        <span className="text-xs text-gray-400">Alert</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              {/* Floating Elements */}
              <div className="absolute -top-4 -right-4 w-24 h-24 bg-neon-blue/20 rounded-full blur-xl"></div>
              <div className="absolute -bottom-4 -left-4 w-24 h-24 bg-neon-green/20 rounded-full blur-xl"></div>
              <div className="absolute top-1/2 -right-8 w-16 h-16 bg-neon-purple/20 rounded-full blur-lg animate-pulse"></div>
            </div>
          </motion.div>
        </div>
      </div>
      
      {/* Floating Tech Elements */}
      <div className="absolute top-20 left-10 w-2 h-2 bg-neon-blue rounded-full animate-bounce" style={{ animationDelay: '0s' }}></div>
      <div className="absolute top-40 right-10 w-2 h-2 bg-neon-green rounded-full animate-bounce" style={{ animationDelay: '1s' }}></div>
      <div className="absolute bottom-20 left-1/4 w-2 h-2 bg-neon-purple rounded-full animate-bounce" style={{ animationDelay: '2s' }}></div>
      <div className="absolute bottom-40 right-1/4 w-2 h-2 bg-neon-red rounded-full animate-bounce" style={{ animationDelay: '3s' }}></div>
    </section>
  )
}

export default Hero