package ru.itmo.qa.lab2.stub;

import ru.itmo.qa.lab2.function.AbstractFunction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class BaseNLogarithmStub extends AbstractFunction {

    private final int base;
    private final Map<Double, Double> table;

    public BaseNLogarithmStub(int base) {
        this.base = base;
        this.table = createTable(base);
    }

    private static Map<Double, Double> createTable(int base) {
        Map<Double, Double> table = new HashMap<>();
        table.put(1.0, 0.0);
        double ln = Math.log(base);
        table.put(0.1,  Math.log(0.1)  / ln);
        table.put(0.5,  Math.log(0.5)  / ln);
        table.put(2.0,  Math.log(2.0)  / ln);
        table.put(3.0,  Math.log(3.0)  / ln);
        table.put(5.0,  Math.log(5.0)  / ln);
        table.put(8.0,  Math.log(8.0)  / ln);
        table.put(10.0, Math.log(10.0) / ln);
        table.put(100.0, Math.log(100.0) / ln);
        table.put((double) base, 1.0);
        return table;
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) {
        if (x.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ArithmeticException("Логарифм не определен для x <= 0");
        }
        double xDouble = x.doubleValue();
        for (Map.Entry<Double, Double> entry : table.entrySet()) {
            if (Math.abs(entry.getKey() - xDouble) < 0.01) {
                return new BigDecimal(entry.getValue()).setScale(precision.scale(), RoundingMode.HALF_EVEN);
            }
        }
        return null;
    }
}
