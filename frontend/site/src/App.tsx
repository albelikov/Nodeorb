import React from 'react'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import LandingPage from './components/LandingPage'
import AuthBridge from './components/AuthBridge'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={
          <AuthBridge>
            <Layout>
              <LandingPage />
            </Layout>
          </AuthBridge>
        } />
      </Routes>
    </Router>
  )
}

export default App