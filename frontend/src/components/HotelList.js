import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './HotelList.css';

function HotelList() {
  const [hotels, setHotels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchHotels();
  }, []);

  const fetchHotels = async () => {
    try {
      const response = await axios.get('/micro/hotels/');
      setHotels(response.data);
      setLoading(false);
    } catch (err) {
      setError('Failed to load hotels. Make sure backend is running on port 9001.');
      setLoading(false);
    }
  };

  if (loading) return <div className="loading">Loading hotels...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="hotel-list">
      <div className="card">
        <h2>🏨 Available Hotels</h2>
        <p>Fetched from Hotel Service via API Gateway</p>
      </div>

      <div className="hotels-grid">
        {hotels.length === 0 ? (
          <div className="card">
            <p>No hotels found. Add some hotels through the API!</p>
          </div>
        ) : (
          hotels.map(hotel => (
            <div key={hotel.hotelId} className="hotel-card">
              <h3>{hotel.name}</h3>
              <p className="location">📍 {hotel.location}</p>
              <p className="about">{hotel.about}</p>
              <div className="hotel-id">ID: {hotel.hotelId}</div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default HotelList;
