import React, { useRef, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { createPoint } from '../redux/slices/pointsSlice';

const DiagramCanvas = () => {
    const dispatch = useDispatch();
    const r = useSelector((state) => state.points.form.r) || 1;

    const points = useSelector((state) => state.points.points);
    const canvasRef = useRef(null);

    const width = 400;
    const height = 400;
    const padding = 40; // Расширенная область клика
    const graphSize = width - 2 * padding;
    const scale = graphSize / (2 * r);
    const centerX = width / 2;
    const centerY = height / 2;

    const toCanvasCoords = (x, y) => {
        return {
            cx: centerX + x * scale,
            cy: centerY - y * scale,
        };
    };

    const drawCanvas = (ctx) => {
        ctx.clearRect(0, 0, width, height);
        ctx.fillStyle = 'white';
        ctx.fillRect(0, 0, width, height);

        ctx.strokeStyle = 'black';
        ctx.lineWidth = 2;

        // Оси X и Y
        ctx.beginPath();
        ctx.moveTo(padding, centerY);
        ctx.lineTo(width - padding, centerY);
        ctx.moveTo(centerX, padding);
        ctx.lineTo(centerX, height - padding);
        ctx.stroke();

        // Подписи осей
        ctx.font = '16px Arial';
        ctx.fillStyle = 'black';
        ctx.textAlign = 'center';
        ctx.fillText('X', width - padding / 2 - 5, centerY);
        ctx.fillText('Y', centerX, padding / 2 + 5);

        // Засечки и подписи
        ctx.font = '14px Arial';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';

        for (let i = -r; i <= r; i += r / 2) {
            if (i !== 0) {
                // Засечки на оси X
                const { cx, cy } = toCanvasCoords(i, 0);
                ctx.beginPath();
                ctx.moveTo(cx, centerY - 5);
                ctx.lineTo(cx, centerY + 5);
                ctx.stroke();
                ctx.fillText(i.toFixed(1), cx, centerY + 20);
            }
        }

        for (let i = -r; i <= r; i += r / 2) {
            if (i !== 0) {
                // Засечки на оси Y
                const { cx, cy } = toCanvasCoords(0, i);
                ctx.beginPath();
                ctx.moveTo(centerX - 5, cy);
                ctx.lineTo(centerX + 5, cy);
                ctx.stroke();
                ctx.fillText(i.toFixed(1), centerX - 20, cy);
            }
        }

        // Закрашенная область
        ctx.fillStyle = 'rgba(30,144,255,0.5)';
        ctx.beginPath();

        // Четверть круга
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, r * scale, Math.PI / 2, -Math.PI, false);

        // Прямоугольник
        // ctx.moveTo(centerX, centerY);
        ctx.lineTo(centerX + r * scale, centerY);
        ctx.lineTo(centerX + r * scale, centerY - (r / 2) * scale);
        ctx.lineTo(centerX, centerY - (r / 2) * scale);

        // Треугольник
        ctx.lineTo(centerX, centerY - r * scale);
        ctx.lineTo(centerX - r * scale, centerY);
        ctx.closePath();
        ctx.fill();

        // Точки
        points.forEach((pt) => {
            const { cx, cy } = toCanvasCoords(pt.x, pt.y);
            let hit;
            if (pt.x <= 0 && pt.y >= 0) {
                hit = (pt.x >= -r) && (pt.y <= r) && (r >= pt.y - pt.x);
            } else if (pt.x >= 0 && pt.y >= 0) {
                hit = (pt.x <= r) && (pt.y <= r / 2);
            } else if (pt.x >= 0 && pt.y <= 0) {
                hit = false;
            } else {
                hit = (pt.x * pt.x + pt.y * pt.y <= r * r);
            }
            ctx.beginPath();
            ctx.arc(cx, cy, 4, 0, 2 * Math.PI);
            ctx.fillStyle = hit ? 'green' : 'red';
            ctx.fill();
        });
    };

    useEffect(() => {
        const canvas = canvasRef.current;
        if (canvas) {
            const ctx = canvas.getContext('2d');
            drawCanvas(ctx);
        }
    }, [points, r]);

    const handleCanvasClick = (e) => {
        const rect = e.currentTarget.getBoundingClientRect();
        const clickX = e.clientX - rect.left;
        const clickY = e.clientY - rect.top;
        const x = ((clickX - centerX) / scale).toFixed(2);
        const y = ((centerY - clickY) / scale).toFixed(2);

        dispatch(createPoint({ x: parseFloat(x), y: parseFloat(y), r }));
    };

    return (
        <div className="diagram-container">
            <h2>Diagram with R = {r}</h2>
            <canvas
                ref={canvasRef}
                width={width}
                height={height}
                onClick={handleCanvasClick}
                style={{ border: '1px solid black', cursor: 'pointer' }}
            />
        </div>
    );
};

export default DiagramCanvas;