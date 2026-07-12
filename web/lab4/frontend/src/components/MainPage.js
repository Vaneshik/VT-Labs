// src/components/MainPage.js
import React from 'react';
import Header from './Header';
import PointForm from './PointForm';
import PointsTable from './PointsTable';
import Diagram from './Diagram';
import { useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { fetchPoints } from '../redux/slices/pointsSlice';

const MainPage = () => {
    const dispatch = useDispatch();
    useEffect(() => {
      dispatch(fetchPoints());
    }, [dispatch]);
    useEffect(() => {
        document.body.style.overflow = "auto";
    
        return () => {
          document.body.style.overflow = "hidden";
        };
      }, []);
  
    return (
        <div className="main-page">
            <Header />
            <div className="main-page-content">
            <main>
                <section className="form-section">
                    <PointForm />
                </section>
                <section className="diagram-section">
                    <Diagram />
                </section>
                <section className="log-section">
                    <PointsTable />
                </section>
            </main>
            </div>
        </div>
    );
};

export default MainPage;