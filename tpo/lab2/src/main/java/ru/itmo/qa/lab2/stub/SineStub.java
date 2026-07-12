package ru.itmo.qa.lab2.stub;

import ru.itmo.qa.lab2.function.AbstractFunction;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class SineStub extends AbstractFunction {

    private static final Map<Double, Double> SINE_TABLE = createSineTable();

    private static Map<Double, Double> createSineTable() {
        Map<Double, Double> table = new HashMap<>();
        table.put(0.0, 0.0);
        table.put(Math.PI / 6, 0.5);
        table.put(Math.PI / 4, Math.sqrt(2) / 2);
        table.put(Math.PI / 3, Math.sqrt(3) / 2);
        table.put(Math.PI / 2, 1.0);
        table.put(2 * Math.PI / 3, Math.sqrt(3) / 2);
        table.put(3 * Math.PI / 4, Math.sqrt(2) / 2);
        table.put(5 * Math.PI / 6, 0.5);
        table.put(Math.PI, 0.0);
        table.put(-Math.PI / 6, -0.5);
        table.put(-Math.PI / 2, -1.0);
        table.put(-Math.PI, 0.0);
        return table;
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) {
        double xDouble = x.doubleValue();
        for (Map.Entry<Double, Double> entry : SINE_TABLE.entrySet()) {
            if (Math.abs(entry.getKey() - xDouble) < 0.01) {
                return new BigDecimal(entry.getValue()).setScale(precision.scale(),
                    java.math.RoundingMode.HALF_EVEN);
            }
        }
        return null;
    }
}
