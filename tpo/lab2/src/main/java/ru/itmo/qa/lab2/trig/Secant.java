package ru.itmo.qa.lab2.trig;

import ru.itmo.qa.lab2.function.AbstractFunction;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.lang.String.format;
import static java.math.RoundingMode.HALF_EVEN;

public class Secant extends AbstractFunction {

    private final AbstractFunction cosine;

    public Secant() {
        super();
        this.cosine = new Cosine();
    }

    public Secant(AbstractFunction cosine) {
        super();
        this.cosine = cosine;
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) throws ArithmeticException {
        isValid(x, precision);

        MathContext mc = new MathContext(precision.scale() + 12, HALF_EVEN);
        BigDecimal cosVal = cosine.calculate(x, precision.setScale(precision.scale() + 12, HALF_EVEN));

        if (cosVal.abs().compareTo(new BigDecimal("0.00000001")) < 0) {
            throw new ArithmeticException(format("У секанса нет значения при x = %s", x));
        }

        return BigDecimal.ONE.divide(cosVal, mc).setScale(precision.scale(), HALF_EVEN);
    }
}
