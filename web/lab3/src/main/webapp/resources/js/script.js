// window.addEventListener('load', function () {
//     const canvas = document.getElementById('diagram');
//     const ctx = canvas.getContext('2d');
//     const scale = 120; // Scale in pixels per unit
//     const CENTER_X = canvas.width / 2;
//     const CENTER_Y = canvas.height / 2;
//
//     // --------------------------------------------------
//     // Draw static axes, markers, and diagram
//     // --------------------------------------------------
//     function drawStaticDiagram() {
//         // Clear the canvas
//         ctx.clearRect(0, 0, canvas.width, canvas.height);
//
//         // Draw axes
//         ctx.strokeStyle = '#333';
//         ctx.lineWidth = 1;
//         ctx.beginPath();
//         // X-axis
//         ctx.moveTo(0, CENTER_Y);
//         ctx.lineTo(canvas.width, CENTER_Y);
//         // Y-axis
//         ctx.moveTo(CENTER_X, 0);
//         ctx.lineTo(CENTER_X, canvas.height);
//         ctx.stroke();
//
//         // Add markers for R and R/2 on axes
//         ctx.fillStyle = '#000';
//         ctx.font = '12px Arial';
//
//         // Positive X-axis
//         ctx.beginPath();
//         ctx.moveTo(CENTER_X + scale, CENTER_Y - 5);
//         ctx.lineTo(CENTER_X + scale, CENTER_Y + 5);
//         ctx.stroke();
//         ctx.fillText('R', CENTER_X + scale - 10, CENTER_Y + 15);
//
//         ctx.beginPath();
//         ctx.moveTo(CENTER_X + (scale / 2), CENTER_Y - 5);
//         ctx.lineTo(CENTER_X + (scale / 2), CENTER_Y + 5);
//         ctx.stroke();
//         ctx.fillText('R/2', CENTER_X + (scale / 2) - 15, CENTER_Y + 15);
//
//         // Negative X-axis
//         ctx.beginPath();
//         ctx.moveTo(CENTER_X - scale, CENTER_Y - 5);
//         ctx.lineTo(CENTER_X - scale, CENTER_Y + 5);
//         ctx.stroke();
//         ctx.fillText('-R', CENTER_X - scale - 15, CENTER_Y + 15);
//
//         ctx.beginPath();
//         ctx.moveTo(CENTER_X - (scale / 2), CENTER_Y - 5);
//         ctx.lineTo(CENTER_X - (scale / 2), CENTER_Y + 5);
//         ctx.stroke();
//         ctx.fillText('-R/2', CENTER_X - (scale / 2) - 25, CENTER_Y + 15);
//
//         // Positive Y-axis
//         ctx.beginPath();
//         ctx.moveTo(CENTER_X - 5, CENTER_Y - scale);
//         ctx.lineTo(CENTER_X + 5, CENTER_Y - scale);
//         ctx.stroke();
//         ctx.fillText('R', CENTER_X + 10, CENTER_Y - scale + 5);
//
//         ctx.beginPath();
//         ctx.moveTo(CENTER_X - 5, CENTER_Y - (scale / 2));
//         ctx.lineTo(CENTER_X + 5, CENTER_Y - (scale / 2));
//         ctx.stroke();
//         ctx.fillText('R/2', CENTER_X + 10, CENTER_Y - (scale / 2) + 5);
//
//         // Negative Y-axis
//         ctx.beginPath();
//         ctx.moveTo(CENTER_X - 5, CENTER_Y + scale);
//         ctx.lineTo(CENTER_X + 5, CENTER_Y + scale);
//         ctx.stroke();
//         ctx.fillText('-R', CENTER_X + 10, CENTER_Y + scale + 5);
//
//         ctx.beginPath();
//         ctx.moveTo(CENTER_X - 5, CENTER_Y + (scale / 2));
//         ctx.lineTo(CENTER_X + 5, CENTER_Y + (scale / 2));
//         ctx.stroke();
//         ctx.fillText('-R/2', CENTER_X + 10, CENTER_Y + (scale / 2) + 5);
//
//         // Draw region (static, assuming R=1 for scaling)
//         ctx.fillStyle = 'rgba(0, 128, 255, 0.2)';
//         // Quarter-circle in top-left
//         ctx.beginPath();
//         ctx.arc(CENTER_X, CENTER_Y, scale, Math.PI / 2, Math.PI);
//         ctx.lineTo(CENTER_X, CENTER_Y);
//         ctx.closePath();
//         ctx.fill();
//
//         // Triangle in bottom-right
//         ctx.beginPath();
//         ctx.moveTo(CENTER_X, CENTER_Y);
//         ctx.lineTo(CENTER_X + scale, CENTER_Y);
//         ctx.lineTo(CENTER_X, CENTER_Y + scale);
//         ctx.closePath();
//         ctx.fill();
//
//         // Rectangle in top-right
//         ctx.beginPath();
//         ctx.rect(CENTER_X, CENTER_Y - (scale / 2), (scale / 2), (scale / 2));
//         ctx.closePath();
//         ctx.fill();
//     }
//
//     // --------------------------------------------------
//     // Draw points dynamically based on R
//     // --------------------------------------------------
//     function drawPoints(points, r) {
//         points.forEach(function (point) {
//             const px = CENTER_X + (point.x / r) * scale; // Adjust for R
//             const py = CENTER_Y + (point.y / r) * scale; // Adjust for R (invert Y-axis)
//
//             ctx.beginPath();
//             ctx.arc(px, py, 3, 0, 2 * Math.PI);
//             ctx.fillStyle = point.hit ? 'green' : 'red'; // Green if hit, red otherwise
//             ctx.fill();
//         });
//     }
//
//     // --------------------------------------------------
//     // Fetch points from the log table
//     // --------------------------------------------------
//     function getPointsFromLogTable() {
//         const rows = document.querySelectorAll('.log-table tbody tr');
//         return Array.from(rows).map((row) => {
//             const cells = row.querySelectorAll('td');
//             return {
//                 x: parseFloat(cells[1].textContent),
//                 y: parseFloat(cells[2].textContent),
//                 r: parseFloat(cells[3].textContent),
//                 hit: cells[4].textContent.trim() === 'Yes'
//             };
//         });
//     }
//
//     // --------------------------------------------------
//     // Reload the canvas
//     // --------------------------------------------------
//     function reloadCanvas() {
//         const hiddenR = document.getElementById('canvasForm:hiddenR');
//         let r = 1;
//         if (hiddenR) {
//             r = Math.max(parseFloat(hiddenR.value), 1);
//         }
//         drawStaticDiagram(); // Draw static region
//         const points = getPointsFromLogTable(); // Get points from log table
//         drawPoints(points, r); // Draw points with dynamic R scaling
//     }
//
//     // Attach `reloadCanvas` to a global function to allow AJAX to trigger it
//     window.updateCanvas = reloadCanvas;
//
//     // Initial load
//     reloadCanvas();
// });