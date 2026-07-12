let selectedRValue = null;
const points = []; // Array to store all points

function selectRValue(value) {
    selectedRValue = value;
    document.getElementById('r-hidden').value = value;

    document.querySelectorAll('.r-buttons button').forEach(button => {
        button.classList.toggle('selected', button.innerText === value.toString());
    });

    drawDiagram(); // Redraw diagram when R is changed
}

function validateYInput(y) {
    const yValue = parseFloat(y);
    return !isNaN(yValue) && yValue >= -3 && yValue <= 3;
}

document.getElementById('point-form').addEventListener('submit', function(event) {
    const yInput = document.getElementById('y-input').value.trim();

    if (!validateYInput(yInput)) {
        alert('Значение Y должно быть числом в диапазоне от -3 до 3.');
        event.preventDefault(); // Предотвратить отправку формы, если проверка не пройдена
        return false;
    }
});

const canvas = document.getElementById('diagram');
const ctx = canvas.getContext('2d');
const canvasSize = 300;
const center = canvasSize / 2;
const R = 100; // Radius in pixels

function drawDiagram() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = 'rgba(0, 123, 255, 0.5)';
    ctx.strokeStyle = 'black';
    ctx.lineWidth = 1;

    // Rectangle in Quadrant III
    ctx.fillRect(center - R, center, R, R);

    // Triangle in Quadrant II
    ctx.beginPath();
    ctx.moveTo(center, center); // Center point
    ctx.lineTo(center - R/2, center); // Left side
    ctx.lineTo(center, center - R / 2); // Top
    ctx.closePath();
    ctx.fill();

    // Sector in Quadrant IV
    ctx.beginPath();
    ctx.moveTo(center, center); // Center point
    ctx.arc(center, center, R / 2, 0, -Math.PI * 1.5, false);
    ctx.closePath();
    ctx.fill();

    // Axes
    ctx.strokeStyle = 'black';
    ctx.beginPath();
    ctx.moveTo(center, 0);
    ctx.lineTo(center, canvasSize); // Y-axis
    ctx.moveTo(0, center);
    ctx.lineTo(canvasSize, center); // X-axis
    ctx.stroke();

    // Arrows
    drawArrow(center, 20, center, 0); // Y-axis arrow
    drawArrow(canvasSize - 20, center, canvasSize, center); // X-axis arrow

    // Ticks and Labels
    ctx.setLineDash([5, 5]);
    ctx.font = '14px Arial';
    ctx.fillStyle = 'black';

    [-R, -R / 2, R / 2, R].forEach(offset => {
        // X-axis ticks and labels
        ctx.beginPath();
        ctx.moveTo(center + offset, center - 5);
        ctx.lineTo(center + offset, center + 5);
        ctx.stroke();
        const xLabel = offset === R ? "R" : offset === R / 2 ? "R/2" : offset === -R / 2 ? "-R/2" : "-R";
        ctx.fillText(xLabel, center + offset - 10, center + 20);

        // Y-axis ticks and labels
        ctx.beginPath();
        ctx.moveTo(center - 5, center + offset);
        ctx.lineTo(center + 5, center + offset);
        ctx.stroke();
        const yLabel = offset === R ? "R" : offset === R / 2 ? "R/2" : offset === -R / 2 ? "-R/2" : "-R";
        ctx.fillText(yLabel, center + 10, center + offset + 5);
    });

    ctx.setLineDash([]);

    // Draw all points
    points.forEach(point => {
        plotPoint(point.x, point.y, selectedRValue, R);
    });
}
function drawArrow(fromX, fromY, toX, toY) {
    const headlen = 10;
    const angle = Math.atan2(toY - fromY, toX - fromX);

    ctx.beginPath();
    ctx.moveTo(fromX, fromY);
    ctx.lineTo(toX, toY);
    ctx.lineTo(
        toX - headlen * Math.cos(angle - Math.PI / 6),
        toY - headlen * Math.sin(angle - Math.PI / 6)
    );
    ctx.moveTo(toX, toY);
    ctx.lineTo(
        toX - headlen * Math.cos(angle + Math.PI / 6),
        toY - headlen * Math.sin(angle + Math.PI / 6)
    );
    ctx.stroke();
}

function plotPoint(x, y, currentR, diagramR) {
    const scale = diagramR / currentR; // Scale points based on the current diagram radius
    const xPos = center + x * scale; // Scale X-coordinate
    const yPos = center - y * scale; // Scale Y-coordinate (inverted for canvas)

    ctx.fillStyle = (x >= -currentR && x <= 0 && y <= 0 && y >= -currentR) ||
        (x <= 0 && y >= 0 && (x >= -currentR/2 && y <= currentR/2 + x)) ||
        (x >= 0 && y <= 0 && (x*x + y*y <= currentR*currentR/4.0)) ? 'green' : 'red';
    ctx.beginPath();
    ctx.arc(xPos, yPos, 4, 0, 2 * Math.PI); // Draw a circle with radius 4 pixels
    ctx.fill();
}

// Function to load points from the log table
function loadPointsFromTable() {
    const rows = document.querySelectorAll('#log-entries tr');
    // console.log('Rows:', rows.length)
    points.length = 0; // Clear previous points

    rows.forEach(row => {
        const cells = row.querySelectorAll('td');
        if (cells.length === 7) { // Ensure the row has data
            const x = parseFloat(cells[1].innerText);
            const y = parseFloat(cells[2].innerText);
            const r = parseFloat(cells[3].innerText);
            const isHit = cells[6].innerText.trim() === 'Да'; // Assume 'Да' means hit

            points.push({ x, y, isHit });
        }
    });

    drawDiagram(); // Redraw the diagram with loaded points
}

canvas.addEventListener('click', function (event) {
    if (!selectedRValue) {
        alert('Выберите значение R перед кликом на диаграмме.');
        return;
    }

    const rect = canvas.getBoundingClientRect();
    const clickX = event.clientX - rect.left;
    const clickY = event.clientY - rect.top;

    const scaledX = ((clickX - center) / R) * selectedRValue;
    const scaledY = ((center - clickY) / R) * selectedRValue;

    sendCoordinates(scaledX.toFixed(2), scaledY.toFixed(2), selectedRValue);
});

function sendCoordinates(x, y, r) {
    const url = `/my-webapp/controller?x=${x}&y=${y}&r=${r}`;
    window.location.href = url; // Send GET request to the server
}

drawDiagram();
loadPointsFromTable();
selectRValue(1);