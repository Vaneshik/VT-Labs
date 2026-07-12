<%@ page import="org.example.model.Result" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Точка на координатной плоскости</title>
    <link rel="stylesheet" href="static/style.css"> <!-- Подключение CSS -->
</head>
<body>
<div class="container">
    <div class="header">
        <div>Р3207</div>
        <div>Чусовлянов Максим Сергеевич</div>
        <div>409858</div>
    </div>

    <div class="main-content">
        <div class="form-section">
            <h3>Проверка попадания точки</h3>
            <form id="point-form" action="controller" method="get">
                <label for="x-select">Изменение X:</label>
                <select id="x-select" name="x">
                    <option value="-5">-5</option>
                    <option value="-4">-4</option>
                    <option value="-3">-3</option>
                    <option value="-2">-2</option>
                    <option value="-1">-1</option>
                    <option value="0" selected>0</option> <!-- Значение по умолчанию -->
                    <option value="1">1</option>
                    <option value="2">2</option>
                    <option value="3">3</option>
                </select>

                <label for="y-input">Изменение Y (от -3 до 3):</label>
                <input type="text" id="y-input" name="y"> <!-- Значение по умолчанию -->

                <label>Изменение R:</label>
                <div class="r-buttons">
                    <button type="button" onclick="selectRValue(1)">1</button> <!-- Значение по умолчанию -->
                    <button type="button" onclick="selectRValue(2)">2</button>
                    <button type="button" onclick="selectRValue(3)">3</button>
                    <button type="button" onclick="selectRValue(4)">4</button>
                    <button type="button" onclick="selectRValue(5)">5</button>
                </div>

                <input type="hidden" id="r-hidden" name="r">

                <button type="submit">Отправить</button>
            </form>
        </div>

        <div class="graphic-and-info">
            <div class="graphic-content">
                <div class="info-container">
                    <div class="current-point-info">
                        <p><span>ID:</span>
                            <span id="point-id">
                        <%
                            List<Result> logResults = (List<Result>) session.getAttribute("resultLog");
                            if (logResults != null && !logResults.isEmpty()) {
                                Result lastResult = logResults.get(logResults.size() - 1);
                                out.print(lastResult.getId());
                            } else {
                                out.print("N/A");
                            }
                        %>
                    </span>
                        </p>
                    </div>
                    <div id="status-icon" class="status-icon">
                        <%
                            if (logResults != null && !logResults.isEmpty()) {
                                Result lastResult = logResults.get(logResults.size() - 1);
                                out.print(lastResult.isHit() ? "✅" : "❌");
                            } else {
                                out.print("❌");
                            }
                        %>
                    </div>
                </div>
                <div class="graphic-section">
                    <canvas id="diagram" width="300" height="300"></canvas>
                </div>
            </div>
        </div>
    </div>

    <div class="log-table-wrapper">
        <div class="log-table">
            <table>
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
                <tbody id="log-entries">
                <%
                    if (logResults != null && !logResults.isEmpty()) {
                        for (int i = logResults.size() - 1; i >= 0; i--) {
                            Result result = logResults.get(i);
                %>
                <tr style="background-color: <%= result.isHit() ? "rgba(76, 175, 80, 0.6)" : "rgba(175, 76, 76, 0.6)" %>">
                    <td><%= result.getId() %></td>
                    <td><%= result.getX() %></td>
                    <td><%= result.getY() %></td>
                    <td><%= result.getR() %></td>
                    <td><%= result.getStartTime() %></td>
                    <td><%= result.getExecutionTime() %></td>
                    <td><%= result.isHit() ? "Да" : "Нет" %></td>
                </tr>
                <%
                    }
                } else {
                %>
                <tr>
                    <td colspan="7">Лог пуст</td>
                </tr>
                <%
                    }
                %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script src="static/script.js"></script>

</body>
</html>