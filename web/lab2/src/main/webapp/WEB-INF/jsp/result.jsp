<%@ page import="org.example.model.Result" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Результат текущей проверки</title>
    <link rel="stylesheet" href="static/style.css">
    <style>
        .result-container {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 20px;
        }
        .result-text {
            font-size: 36px;
            font-weight: bold;
            color: #4CAF50;
            margin-top: 20px;
        }
        .result-text.miss {
            color: #FF4C4C;
        }
        .gif-container {
            flex: 1;
            display: flex;
            justify-content: center;
        }
        .gif-container img {
            width: 300px;
            height: auto;
        }
    </style>

    <div class="container">
        <h1>Результат текущей проверки</h1>
        <div class="result-container">
            <table border="1">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>X</th>
                    <th>Y</th>
                    <th>R</th>
                    <th>Время начала</th>
                    <th>Время выполнения (мс)</th>
                    <th>Попадание</th>
                </tr>
                </thead>
                <tbody>
                <%
                    Result currentResult = (Result) request.getAttribute("currentResult");
                    if (currentResult != null) {
                %>
                <tr>
                    <td><%= currentResult.getId() %></td>
                    <td><%= currentResult.getX() %></td>
                    <td><%= currentResult.getY() %></td>
                    <td><%= currentResult.getR() %></td>
                    <td><%= currentResult.getStartTime() %></td>
                    <td><%= currentResult.getExecutionTime() %></td>
                    <td><%= currentResult.isHit() ? "Да" : "Нет" %></td>
                </tr>
                <%
                } else {
                %>
                <tr>
                    <td colspan="7">Нет данных для отображения</td>
                </tr>
                <%
                    }
                %>
                </tbody>
            </table>
            <div class="gif-container">
                <img src="<%= currentResult != null && currentResult.isHit() ? "static/hit.gif" : "static/miss.gif" %>" alt="Result GIF">
            </div>
        </div>

        <div class="result-text <%= currentResult != null && !currentResult.isHit() ? "miss" : "" %>">
            <%= currentResult != null && currentResult.isHit() ? "ПОПАДАНИЕ!" : "ПРОМАХ!" %>
        </div>

        <a href=".">Назад к форме</a>
    </div>

<script>
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            window.location.href = '.';
        }
    });
</script>
</body>
</html>