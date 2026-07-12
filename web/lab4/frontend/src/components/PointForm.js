// src/components/PointForm.js
import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { createPoint, setFormField, setFormError, resetForm } from '../redux/slices/pointsSlice';
import { Button, TextInput } from 'belle';

const PointForm = () => {
  const dispatch = useDispatch();

  // Get fields from Redux
  const { x, y, r, error } = useSelector((state) => state.points.form);

  const validateInputs = () => {
    const numX = parseFloat(x);
    const numY = parseFloat(y);
    const numR = parseFloat(r);

    if (isNaN(numX) || numX < -5 || numX > 5) {
      return 'X coordinate must be a number between -5 and 5.';
    }
    if (isNaN(numY) || numY < -5 || numY > 3) {
      return 'Y coordinate must be a number between -5 and 3.';
    }
    if (isNaN(numR) || numR < 1 || numR > 5) {
      return 'Radius must be a positive number (1 to 5).';
    }
    return '';
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const errorMsg = validateInputs();
    if (errorMsg) {
      dispatch(setFormError(errorMsg));
      return;
    }
    dispatch(setFormError(''));

    dispatch(createPoint({
      x: parseFloat(x),
      y: parseFloat(y),
      r: parseFloat(r),
    }));

    dispatch(resetForm());
  };

  // Handler for Y spinner input
  const handleChangeY = (e) => {
    dispatch(setFormField({ field: 'y', value: e.target.value }));
  };

  // Handler for Radius
  const handleChangeRadius = (e) => {
    dispatch(setFormField({ field: 'r', value: parseInt(e.target.value || 0) }));
  };

  return (
    <form className="point-form" onSubmit={handleSubmit}>
      <div>
        <label>
          X coordinate (-5 to 5):
          <TextInput
            value={x}
            onChange={(e) => dispatch(setFormField({ field: 'x', value: e.target.value }))}
            type="number"
            step="0.1"
            required
          />
        </label>
      </div>
      <div>
        <label>
          Y coordinate (-5 to 3):
          <input
            value={y}
            onChange={handleChangeY}
            type="number"
            min="-5"
            max="3"
            step="0.1"
            className="spinner"
            required
          />
        </label>
      </div>
      <div>
        <label>
          Radius (1 to 5):
          <TextInput
            value={r || 0}
            onChange={handleChangeRadius}
            type="number"
            step="0.1"
            required
          />
        </label>
      </div>
      {error && <div className="error">{error}</div>}
      <Button type="submit">Send Point</Button>
    </form>
  );
};

export default PointForm;