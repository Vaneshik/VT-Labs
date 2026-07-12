package ru.itmo.qa.lab2.stub;

import ru.itmo.qa.lab2.function.AbstractFunction;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class NaturalLogarithmStub extends AbstractFunction {

    private static final Map<Double, Double> LN_TABLE = createLnTable();

    private static Map<Double, Double> createLnTable() {
        Map<Double, Double> table = new HashMap<>();
        table.put(0.1, Math.log(0.1));
        table.put(0.5, Math.log(0.5));
        table.put(1.0, 0.0);
        table.put(Math.E, 1.0);
        table.put(2.0, Math.log(2.0));
        table.put(2.718, 0.999);
        table.put(5.0, Math.log(5.0));
        table.put(10.0, Math.log(10.0));
        return table;
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) {
        if (x.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ArithmeticException("Логарифм не определен для x <= 0");
        }

        double xDouble = x.doubleValue();
        for (Map.Entry<Double, Double> entry : LN_TABLE.entrySet()) {
            if (Math.abs(entry.getKey() - xDouble) < 0.05) {
                return new BigDecimal(entry.getValue()).setScale(precision.scale(),
                    java.math.RoundingMode.HALF_EVEN);
            }
        }
        return null;
    }
}
