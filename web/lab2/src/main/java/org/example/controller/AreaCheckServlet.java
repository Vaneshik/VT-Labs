package org.example.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.model.Result;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/areaCheck")
public class AreaCheckServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            double x = Double.parseDouble(request.getParameter("x"));
            double y = Double.parseDouble(request.getParameter("y"));
            double r = Double.parseDouble(request.getParameter("r"));

            // Validate Y
            if (y < -3 || y > 3) {
                request.setAttribute("errorMessage", "Значение Y должно быть в диапазоне от -3 до 3.");
                RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
                dispatcher.forward(request, response);
                return;
            }

            LocalDateTime startTime = LocalDateTime.now();
            long start = System.currentTimeMillis();

            boolean isHit = checkArea(x, y, r);
            long executionTime = System.currentTimeMillis() - start;

            Result result = new Result(x, y, r, isHit, startTime, executionTime);
            request.setAttribute("currentResult", result);

            HttpSession session = request.getSession();
            List<Result> resultLog = (List<Result>) session.getAttribute("resultLog");
            if (resultLog == null) {
                resultLog = new ArrayList<>();
                session.setAttribute("resultLog", resultLog);
            }
            resultLog.add(result);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/result.jsp");
            dispatcher.forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Некорректные параметры.");
        }
    }

    private boolean checkArea(double x, double y, double r) {
        return (x >= -r && x <= 0 && y <= 0 && y >= -r) ||
                (x <= 0 && y >= 0 && (x >= -r/2 && y <= r/2 + x)) ||
                (x >= 0 && y <= 0 && (x*x + y*y <= r*r/4.0));
    }
}