package ru.itmo.qa.lab2.trig;

import ch.obermuhlner.math.big.BigDecimalMath;
import ru.itmo.qa.lab2.function.AbstractFunction;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.math.RoundingMode.HALF_EVEN;

public class Cosine extends AbstractFunction {

    private final AbstractFunction sine;

    public Cosine() {
        super();
        this.sine = new Sine();
    }

    public Cosine(AbstractFunction sine) {
        super();
        this.sine = sine;
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) throws ArithmeticException {
        isValid(x, precision);

        MathContext mc = new MathContext(precision.scale() + 2, HALF_EVEN);

        BigDecimal piHalf = BigDecimalMath.pi(mc)
                .divide(BigDecimal.valueOf(2), mc.getPrecision(), HALF_EVEN);

        return sine.calculate(piHalf.subtract(x), precision);
    }
}
