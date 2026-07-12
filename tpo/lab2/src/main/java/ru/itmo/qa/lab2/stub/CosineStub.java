package ru.itmo.qa.lab2.stub;

import ru.itmo.qa.lab2.function.AbstractFunction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class CosineStub extends AbstractFunction {

    private static final Map<Double, Double> COSINE_TABLE = createCosineTable();

    private static Map<Double, Double> createCosineTable() {
        Map<Double, Double> table = new HashMap<>();
        table.put(-Math.PI,     -1.0);
        table.put(-Math.PI / 2,  0.0);
        table.put(-1.0,          0.5403023);
        table.put(-0.5,          0.8775826);
        table.put(0.0,           1.0);
        table.put(0.5,           0.8775826);
        table.put(1.0,           0.5403023);
        table.put(2.0,          -0.4161468);
        table.put(Math.PI / 2,   0.0);
        table.put(Math.PI,      -1.0);
        return table;
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) {
        double xDouble = x.doubleValue();
        for (Map.Entry<Double, Double> entry : COSINE_TABLE.entrySet()) {
            if (Math.abs(entry.getKey() - xDouble) < 0.01) {
                return new BigDecimal(entry.getValue()).setScale(precision.scale(), RoundingMode.HALF_EVEN);
            }
        }
        return null;
    }
}
