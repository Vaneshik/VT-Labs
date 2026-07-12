package ru.itmo.qa.lab2.log;

import ru.itmo.qa.lab2.function.AbstractFunction;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static java.lang.String.format;

public class BaseNLogarithm extends AbstractFunction {

    private final NaturalLogarithm naturalLogarithm;
    private final int base;

    public BaseNLogarithm() {
        super();
        this.naturalLogarithm = new NaturalLogarithm();
        this.base = 10;
    }

    public BaseNLogarithm(final int base) {
        super();
        this.naturalLogarithm = new NaturalLogarithm();
        this.base = base;
    }

    public BaseNLogarithm(final int base, final NaturalLogarithm naturalLogarithm) {
        super();
        this.naturalLogarithm = naturalLogarithm;
        this.base = base;
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) throws ArithmeticException {
        isValid(x, precision);
        if (x.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ArithmeticException(format("Логарифм с основанием %s не имеет значения при x = %s", base, x));
        }
        final BigDecimal result = naturalLogarithm.calculate(x, precision).divide(
                naturalLogarithm.calculate(new BigDecimal(base), precision),
                MathContext.DECIMAL128.getPrecision(),
                RoundingMode.HALF_EVEN);
        return result.setScale(precision.scale(), RoundingMode.HALF_EVEN);
    }

    public int getBase() {
        return base;
    }
}
