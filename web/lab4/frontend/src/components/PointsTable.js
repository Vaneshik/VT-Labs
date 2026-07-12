import React from 'react';
import { useSelector } from 'react-redux';

const formatDate = (timestamp) => {
  if (!timestamp) return '-';
  const date = new Date(timestamp);
  return date.toLocaleString('en-GB', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
};

const PointsTable = () => {
  const points = useSelector((state) => state.points.points);

  // Sort by checkTime (newest first)
  const sortedPoints = [...points].sort(
    (a, b) => new Date(b.checkTime) - new Date(a.checkTime)
  );

  return (
    <div className="points-table">
      <h2>Points Log</h2>
      <table>
        <thead>
          <tr>
            <th>Timestamp</th>
            <th>X</th>
            <th>Y</th>
            <th>Radius</th>
            <th>Result</th>
          </tr>
        </thead>
        <tbody>
          {sortedPoints.map((point, index) => (
            <tr key={index}>
              <td>{formatDate(point.checkTime)}</td>
              <td>{point.x.toFixed(2)}</td>
              <td>{point.y.toFixed(2)}</td>
              <td>{point.r.toFixed(0)}</td>
              <td style={{ color: point.hit ? 'green' : 'red' }}>
                {point.hit ? 'Hit' : 'Miss'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default PointsTable;