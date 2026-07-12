package ru.itmo.qa.lab2.trig;

import ch.obermuhlner.math.big.BigDecimalMath;
import ru.itmo.qa.lab2.function.AbstractFunction;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.lang.String.format;
import static java.math.RoundingMode.HALF_EVEN;

public class Cosecant extends AbstractFunction {

    private final AbstractFunction sine;

    public Cosecant() {
        super();
        this.sine = new Sine();
    }

    public Cosecant(AbstractFunction sine) {
        super();
        this.sine = sine;
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) throws ArithmeticException {
        isValid(x, precision);

        MathContext mc = new MathContext(precision.scale() + 12, HALF_EVEN);

        BigDecimal pi = BigDecimalMath.pi(mc);
        BigDecimal normalized = x.remainder(pi.multiply(BigDecimal.valueOf(2), mc));
        if (normalized.abs().compareTo(precision) < 0 ||
            normalized.subtract(pi).abs().compareTo(precision) < 0 ||
            normalized.add(pi).abs().compareTo(precision) < 0) {
            throw new ArithmeticException(format("У косеканса нет значения при x = %s", x));
        }

        BigDecimal sinVal = sine.calculate(x, precision.setScale(precision.scale() + 12, HALF_EVEN));

        if (sinVal.abs().compareTo(precision.divide(BigDecimal.TEN, mc)) < 0) {
            throw new ArithmeticException(format("У косеканса нет значения при x = %s", x));
        }

        return BigDecimal.ONE.divide(sinVal, mc).setScale(precision.scale(), HALF_EVEN);
    }
}
