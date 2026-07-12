package ru.itmo.qa.lab2.trig;

import ch.obermuhlner.math.big.BigDecimalMath;
import ru.itmo.qa.lab2.function.AbstractFunction;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.lang.String.format;
import static java.math.RoundingMode.HALF_EVEN;

public class Cotangent extends AbstractFunction {

    private final AbstractFunction sine;
    private final AbstractFunction cosine;

    public Cotangent() {
        super();
        this.sine = new Sine();
        this.cosine = new Cosine();
    }

    public Cotangent(AbstractFunction sine, AbstractFunction cosine) {
        super();
        this.sine = sine;
        this.cosine = cosine;
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) throws ArithmeticException {
        isValid(x, precision);

        MathContext mc = new MathContext(precision.scale() + 12, HALF_EVEN);
        BigDecimal highPrecision = precision.setScale(precision.scale() + 12, HALF_EVEN);
        BigDecimal sinVal = sine.calculate(x, highPrecision);
        BigDecimal cosVal = cosine.calculate(x, highPrecision);

        if (sinVal.compareTo(BigDecimal.ZERO) == 0 ||
            sinVal.abs().compareTo(new BigDecimal("0.000001")) < 0) {
            throw new ArithmeticException(format("У котангенса нет значения при x = %s", x));
        }

        return cosVal.divide(sinVal, mc).setScale(precision.scale(), HALF_EVEN);
    }
}
