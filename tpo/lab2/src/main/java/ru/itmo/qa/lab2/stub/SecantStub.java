package ru.itmo.qa.lab2.stub;

import ru.itmo.qa.lab2.function.AbstractFunction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class SecantStub extends AbstractFunction {

    private static final Map<Double, Double> SECANT_TABLE = createSecantTable();

    private static Map<Double, Double> createSecantTable() {
        Map<Double, Double> table = new HashMap<>();
        table.put(-Math.PI, -1.0);
        table.put(-1.0,      1.8508157);
        table.put(0.0,       1.0);
        table.put(1.0,       1.8508157);
        table.put(2.0,      -2.4029980);
        table.put(Math.PI,  -1.0);
        return table;
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) {
        double xDouble = x.doubleValue();
        for (Map.Entry<Double, Double> entry : SECANT_TABLE.entrySet()) {
            if (Math.abs(entry.getKey() - xDouble) < 0.01) {
                return new BigDecimal(entry.getValue()).setScale(precision.scale(), RoundingMode.HALF_EVEN);
            }
        }
        return null;
    }
}
