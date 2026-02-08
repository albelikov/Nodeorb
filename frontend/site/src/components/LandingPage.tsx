import React from 'react'
import { ArrowRight, Shield, Lock, Database, Users, Truck, Warehouse, Globe, Zap, Settings, BarChart3, Clock } from 'lucide-react'

const LandingPage: React.FC = () => {
  const services = [
    { name: 'Marketplace', icon: Globe, description: 'Freight marketplace' },
    { name: 'WMS', icon: Warehouse, description: 'Warehouse management' },
    { name: 'TMS', icon: Truck, description: 'Transportation management' },
    { name: 'FMS', icon: Settings, description: 'Fleet management' },
    { name: 'ERP', icon: Database, description: 'Enterprise resource planning' },
    { name: 'CRM', icon: Users, description: 'Customer relationship management' },
    { name: 'Analytics', icon: BarChart3, description: 'Business analytics' },
    { name: 'Inventory', icon: Database, description: 'Inventory tracking' },
    { name: 'Fleet', icon: Truck, description: 'Fleet operations' },
    { name: 'Documents', icon: Clock, description: 'Document management' },
    { name: 'Support', icon: Shield, description: 'Customer support' }
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      {/* Hero Section */}
      <section className="relative min-h-screen flex items-center justify-center overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-r from-blue-600/20 to-purple-600/20"></div>
        <div className="relative z-10 text-center px-4 sm:px-6 lg:px-8">
          <h1 className="text-4xl sm:text-6xl lg:text-7xl font-bold text-white mb-6">
            Майбутнє безпечної
            <span className="block text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-400">
              логістики
            </span>
          </h1>
          <p className="text-lg sm:text-xl text-slate-300 mb-8 max-w-2xl mx-auto">
            Прозорість, безпека та надійність на кожному етапі логістичного ланцюга.
            Наша система забезпечує незмінність даних та біометричний захист кожної операції.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <button className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-8 py-3 rounded-lg font-semibold hover:from-blue-700 hover:to-purple-700 transition-all duration-300 transform hover:scale-105">
              Дізнатися більше
            </button>
            <button 
              className="border-2 border-white text-white px-8 py-3 rounded-lg font-semibold hover:bg-white hover:text-slate-900 transition-all duration-300"
              onClick={() => window.location.href = 'http://localhost:5173'}
            >
              Перейти до порталу
            </button>
          </div>
        </div>
      </section>

      {/* Services Section */}
      <section className="py-20 bg-slate-800/50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl sm:text-4xl font-bold text-white mb-4">Наші сервіси</h2>
            <p className="text-slate-300 text-lg">Повний спектр логістичних рішень для вашого бізнесу</p>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
            {services.map((service, index) => (
              <div key={index} className="bg-white/5 backdrop-blur-sm rounded-xl p-6 hover:bg-white/10 transition-all duration-300 border border-white/10">
                <service.icon className="w-12 h-12 text-blue-400 mb-4" />
                <h3 className="text-xl font-semibold text-white mb-2">{service.name}</h3>
                <p className="text-slate-300">{service.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* SCM Protection Section */}
      <section className="py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl sm:text-4xl font-bold text-white mb-4">The SCM Advantage</h2>
            <p className="text-slate-300 text-lg">Безкомпромісна безпека та захист ваших даних</p>
          </div>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="bg-gradient-to-br from-blue-600/20 to-blue-800/20 p-8 rounded-xl border border-blue-500/30">
              <Lock className="w-12 h-12 text-blue-400 mb-4" />
              <h3 className="text-xl font-semibold text-white mb-4">Незмінні логи (WORM)</h3>
              <p className="text-slate-300">
                Всі операції записуються в незмінний журнал, який забезпечує повну прозорість
                та аудит кожного дії в системі.
              </p>
            </div>
            <div className="bg-gradient-to-br from-green-600/20 to-green-800/20 p-8 rounded-xl border border-green-500/30">
              <Shield className="w-12 h-12 text-green-400 mb-4" />
              <h3 className="text-xl font-semibold text-white mb-4">Біометричний підпис</h3>
              <p className="text-slate-300">
                Кожна дія в системі підтверджується біометричними даними користувача,
                забезпечуючи максимальний рівень безпеки.
              </p>
            </div>
            <div className="bg-gradient-to-br from-purple-600/20 to-purple-800/20 p-8 rounded-xl border border-purple-500/30">
              <Zap className="w-12 h-12 text-purple-400 mb-4" />
              <h3 className="text-xl font-semibold text-white mb-4">Захист від фроду</h3>
              <p className="text-slate-300">
                Сучасні алгоритми аналізу поведінки та AI-системи запобігають шахрайству
                та недозволеним діям в реальному часі.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900">
        <div className="max-w-4xl mx-auto text-center px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl sm:text-4xl font-bold text-white mb-6">
            Готові почати роботу з безпечним логістичним рішенням?
          </h2>
          <p className="text-slate-300 text-lg mb-8">
            Приєднуйтесь до майбутнього логістики вже сьогодні
          </p>
          <button className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-12 py-4 rounded-lg font-semibold text-lg hover:from-blue-700 hover:to-purple-700 transition-all duration-300 transform hover:scale-105">
            Перейти до порталу
          </button>
        </div>
      </section>
    </div>
  )
}

export default LandingPage