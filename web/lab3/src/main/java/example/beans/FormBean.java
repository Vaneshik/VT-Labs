package example.beans;

import example.db.ResultDAO;
import example.db.ResultDAOImpl;
import example.entity.ResultEntity;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.*;

@Named("bean")
@SessionScoped
public class FormBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ResultDAO resultDAO = new ResultDAOImpl();


    private Map<String, Boolean> xValues = new HashMap<>();

    private double yValue;

    private double radius;

    private ResultEntity lastResult;

    private List<ResultEntity> results;

    private double clickX;
    private double clickY;

    public double getClickX() {
        return clickX;
    }

    public void setClickX(double clickX) {
        this.clickX = clickX;
    }

    public double getClickY() {
        return clickY;
    }

    public void setClickY(double clickY) {
        this.clickY = clickY;
    }

    public FormBean() {
        xValues.put("-2", false);
        xValues.put("-1.5", false);
        xValues.put("-1", false);
        xValues.put("-0.5", false);
        xValues.put("0", false);
        xValues.put("0.5", false);
        xValues.put("1", false);
        xValues.put("1.5", false);
        xValues.put("2", false);
    }

    public Map<String, Boolean> getXValues() {
        return xValues;
    }

    public void setXValues(Map<String, Boolean> updatedMap) {
        String newlyCheckedKey = null;
        for (Map.Entry<String, Boolean> entry : updatedMap.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                newlyCheckedKey = entry.getKey();
                break;
            }
        }

        if (newlyCheckedKey != null) {
            for (Map.Entry<String, Boolean> entry : updatedMap.entrySet()) {
                if (!entry.getKey().equals(newlyCheckedKey)) {
                    entry.setValue(false);
                }
            }
        }

        this.xValues = updatedMap;
    }

    public double getYValue() {
        return yValue;
    }

    public void setYValue(double yValue) {
        if (yValue < -3) {
            this.yValue = -3;
        } else if (yValue > 3) {
            this.yValue = 3;
        } else {
            this.yValue = yValue;
        }
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean checkHit(double x, double y, double r) {
        if (x < 0 && y < 0) {
            return false;
        }
        return (x >= 0 && y >= 0 && r/2 >= (x + y)) || (x >= 0 && 0 >= y && r/2 >= x && y > -r) || (0 >= x && y >= 0 && r*r >= x*x + y*y);
    }

    private String findSelectedX() {
        for (Map.Entry<String, Boolean> entry : xValues.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String processForm() {
//        System.out.println("Selected xvalues:");
        for (Map.Entry<String, Boolean> entry : xValues.entrySet()) {
            if (entry.getValue()) {
                System.out.println(entry.getKey());
            }
        }
//        System.out.println("Y value: " + yValue);
//        System.out.println("r: " + radius);

        return null;
    }

    public List<ResultEntity> getResultsFromDB() {
        this.results = new ArrayList<>(resultDAO.getAllResults());
        this.results.sort(Comparator.comparing(ResultEntity::getId));
        Collections.reverse(this.results);
        return this.results;
    }

    public ResultEntity getLastResult() {
        return lastResult;
    }

    public void setLastResult(ResultEntity lastResult) {
        this.lastResult = lastResult;
    }

    public void savePoint() {
        String selectedX = findSelectedX();
        double x = selectedX != null ? Double.parseDouble(selectedX) : 0.0;

        ResultEntity entity = new ResultEntity(x, getYValue(), getRadius(), checkHit(clickX, clickY, radius));

        this.lastResult = entity;

        resultDAO.addNewResult(entity);

        getResultsFromDB();
    }

    public String getPointsAsJson() {
        StringBuilder jsonBuilder = new StringBuilder("[");
        for (ResultEntity result : getResultsFromDB()) {
            jsonBuilder.append("{")
                    .append("\"id\":").append(result.getId()).append(",")
                    .append("\"x\":").append(result.getX()).append(",")
                    .append("\"y\":").append(result.getY()).append(",")
                    .append("\"r\":").append(result.getR()).append(",")
                    .append("\"hit\":").append(result.isHit())
                    .append("},");
        }

        if (jsonBuilder.length() > 1) {
            jsonBuilder.setLength(jsonBuilder.length() - 1);
        }

        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    public void savePointFromClick() {
        System.out.println("Clicked at: " + clickX + ", " + clickY + " with R=" + radius);
        double x = clickX;
        double y = clickY;
        boolean isHit = checkHit(x, y, radius);
        ResultEntity entity = new ResultEntity(x, y, radius, isHit);
        resultDAO.addNewResult(entity);
        getResultsFromDB();

        this.lastResult = entity;
    }
}