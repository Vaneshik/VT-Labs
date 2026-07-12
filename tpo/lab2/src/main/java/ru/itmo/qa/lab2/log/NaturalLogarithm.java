package ru.itmo.qa.lab2.log;

import ru.itmo.qa.lab2.function.AbstractFunction;

import java.math.BigDecimal;

import static java.lang.String.format;
import static java.math.RoundingMode.HALF_EVEN;

public class NaturalLogarithm extends AbstractFunction {

    public NaturalLogarithm() {
        super();
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) throws ArithmeticException {
        isValid(x, precision);
        if (x.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ArithmeticException(format("Натуральный логарифм не имеет значения при x = %s", x));
        }
        if (x.compareTo(BigDecimal.ONE) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal z = x.subtract(BigDecimal.ONE).divide(x.add(BigDecimal.ONE), precision.scale() + 2, HALF_EVEN);
        BigDecimal z2 = z.pow(2);
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal term = z;
        int i = 1;
        do {
            result = result.add(term.divide(BigDecimal.valueOf(i), precision.scale() + 2, HALF_EVEN));
            term = term.multiply(z2);
            i += 2;
        } while (term.abs().compareTo(precision) > 0 && i < getSeriesLength());

        return result.multiply(BigDecimal.valueOf(2)).setScale(precision.scale(), HALF_EVEN);
    }
}
