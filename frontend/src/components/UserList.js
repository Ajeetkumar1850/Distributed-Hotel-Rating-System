import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './UserList.css';

function UserList() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      // Note: May require authentication
      const response = await axios.get('/micro/users/', {
        auth: {
          username: 'user',
          password: 'password'
        }
      });
      setUsers(response.data);
      setLoading(false);
    } catch (err) {
      setError('Failed to load users. Authentication required (user/password).');
      setLoading(false);
    }
  };

  if (loading) return <div className="loading">Loading users...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="user-list">
      <div className="card">
        <h2>👥 Registered Users</h2>
        <p>Fetched from User Service (Spring Security Protected)</p>
      </div>

      <div className="users-grid">
        {users.length === 0 ? (
          <div className="card">
            <p>No users found or authentication required.</p>
          </div>
        ) : (
          users.map(user => (
            <div key={user.userId} className="user-card">
              <h3>{user.name}</h3>
              <p>📧 {user.email}</p>
              <p>📱 {user.phone}</p>
              <p className="about">{user.about}</p>
              {user.ratings && (
                <div className="ratings-count">
                  ⭐ {user.ratings.length} Reviews
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default UserList;
