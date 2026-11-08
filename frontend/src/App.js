import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import './App.css';
import HotelList from './components/HotelList';
import UserList from './components/UserList';
import AIRecommendations from './components/AIRecommendations';

function App() {
  const [systemStatus, setSystemStatus] = useState('Checking...');

  useEffect(() => {
    // Check if backend is running
    fetch('/micro/users/health')
      .then(res => res.ok ? setSystemStatus('🟢 Online') : setSystemStatus('🔴 Offline'))
      .catch(() => setSystemStatus('🔴 Offline'));
  }, []);

  return (
    <Router>
      <div className="App">
        <nav className="navbar">
          <div className="nav-container">
            <h1 className="logo">🏨 Hotel Rating System</h1>
            <div className="nav-links">
              <Link to="/">Hotels</Link>
              <Link to="/users">Users</Link>
              <Link to="/ai">AI Recommendations</Link>
            </div>
            <div className="status">Backend: {systemStatus}</div>
          </div>
        </nav>

        <main className="main-content">
          <Routes>
            <Route path="/" element={<HotelList />} />
            <Route path="/users" element={<UserList />} />
            <Route path="/ai" element={<AIRecommendations />} />
          </Routes>
        </main>

        <footer className="footer">
          <p>Built with Spring Boot Microservices + React</p>
          <p>Architecture: API Gateway | Eureka | MongoDB | Redis | Spring AI</p>
        </footer>
      </div>
    </Router>
  );
}

export default App;
