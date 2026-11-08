import React, { useState } from 'react';
import axios from 'axios';
import './AIRecommendations.css';

function AIRecommendations() {
  const [query, setQuery] = useState('');
  const [recommendations, setRecommendations] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;

    setLoading(true);
    setError(null);

    try {
      const response = await axios.post('/micro/ai/recommend', { query });
      setRecommendations(response.data);
      setLoading(false);
    } catch (err) {
      setError('Failed to get AI recommendations. Make sure AI Service is running.');
      setLoading(false);
    }
  };

  return (
    <div className="ai-recommendations">
      <div className="card">
        <h2>🤖 AI-Powered Hotel Recommendations</h2>
        <p>Using Spring AI + Google Gemini for intelligent search</p>
        
        <form onSubmit={handleSearch} className="search-form">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="e.g., luxury hotel near beach with spa"
            className="search-input"
          />
          <button type="submit" className="btn" disabled={loading}>
            {loading ? 'Searching...' : 'Get Recommendations'}
          </button>
        </form>

        <div className="examples">
          <p>Try these queries:</p>
          <div className="example-tags">
            <span onClick={() => setQuery('luxury hotel near beach')}>luxury hotel near beach</span>
            <span onClick={() => setQuery('budget friendly hotel')}>budget friendly hotel</span>
            <span onClick={() => setQuery('romantic hotel with spa')}>romantic hotel with spa</span>
          </div>
        </div>
      </div>

      {error && <div className="error">{error}</div>}

      {recommendations && (
        <div className="recommendations-result">
          <div className="card">
            <h3>Your Query: "{recommendations.query}"</h3>
            <p className="explanation">{recommendations.explanation}</p>
          </div>

          {recommendations.recommendations && recommendations.recommendations.map((rec, index) => (
            <div key={index} className="recommendation-card">
              <div className="rank">#{index + 1}</div>
              <h3>{rec.name}</h3>
              <p className="location">📍 {rec.location}</p>
              <p className="rating">⭐ {rec.averageRating.toFixed(1)} ({rec.totalReviews} reviews)</p>
              <p className="reason">💡 <strong>AI Reason:</strong> {rec.reason}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default AIRecommendations;
