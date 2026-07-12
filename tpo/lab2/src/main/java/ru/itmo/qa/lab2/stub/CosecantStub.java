package ru.itmo.qa.lab2.stub;

import ru.itmo.qa.lab2.function.AbstractFunction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class CosecantStub extends AbstractFunction {

    private static final Map<Double, Double> COSECANT_TABLE = createCosecantTable();

    private static Map<Double, Double> createCosecantTable() {
        Map<Double, Double> table = new HashMap<>();
        table.put(-2.0,          -1.0997502);
        table.put(-Math.PI / 2,  -1.0);
        table.put(-1.0,          -1.1883951);
        table.put(-0.5,          -2.0858296);
        table.put(0.5,            2.0858296);
        table.put(1.0,            1.1883951);
        table.put(Math.PI / 2,    1.0);
        table.put(2.0,            1.0997502);
        return table;
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) {
        double xDouble = x.doubleValue();
        for (Map.Entry<Double, Double> entry : COSECANT_TABLE.entrySet()) {
            if (Math.abs(entry.getKey() - xDouble) < 0.01) {
                return new BigDecimal(entry.getValue()).setScale(precision.scale(), RoundingMode.HALF_EVEN);
            }
        }
        return null;
    }
}
