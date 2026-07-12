package ru.itmo.qa.lab2.stub;

import ru.itmo.qa.lab2.function.AbstractFunction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class CotangentStub extends AbstractFunction {

    private static final Map<Double, Double> COTANGENT_TABLE = createCotangentTable();

    private static Map<Double, Double> createCotangentTable() {
        Map<Double, Double> table = new HashMap<>();
        table.put(-2.0,  0.4576576);
        table.put(-1.0, -0.6420926);
        table.put(-0.5, -1.8304877);
        table.put(0.5,   1.8304877);
        table.put(1.0,   0.6420926);
        table.put(2.0,  -0.4576576);
        return table;
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) {
        double xDouble = x.doubleValue();
        for (Map.Entry<Double, Double> entry : COTANGENT_TABLE.entrySet()) {
            if (Math.abs(entry.getKey() - xDouble) < 0.01) {
                return new BigDecimal(entry.getValue()).setScale(precision.scale(), RoundingMode.HALF_EVEN);
            }
        }
        return null;
    }
}
