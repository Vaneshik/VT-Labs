package ru.itmo.qa.lab2.stub;

import ru.itmo.qa.lab2.function.AbstractFunction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class TangentStub extends AbstractFunction {

    private static final Map<Double, Double> TANGENT_TABLE = createTangentTable();

    private static Map<Double, Double> createTangentTable() {
        Map<Double, Double> table = new HashMap<>();
        table.put(-Math.PI,  0.0);
        table.put(-1.0,     -1.5574077);
        table.put(-0.5,     -0.5463025);
        table.put(0.0,       0.0);
        table.put(0.5,       0.5463025);
        table.put(1.0,       1.5574077);
        table.put(Math.PI,   0.0);
        return table;
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) {
        double xDouble = x.doubleValue();
        for (Map.Entry<Double, Double> entry : TANGENT_TABLE.entrySet()) {
            if (Math.abs(entry.getKey() - xDouble) < 0.01) {
                return new BigDecimal(entry.getValue()).setScale(precision.scale(), RoundingMode.HALF_EVEN);
            }
        }
        return null;
    }
}
