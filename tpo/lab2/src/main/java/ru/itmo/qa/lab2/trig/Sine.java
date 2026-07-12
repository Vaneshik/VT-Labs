package ru.itmo.qa.lab2.trig;

import ch.obermuhlner.math.big.BigDecimalMath;
import ru.itmo.qa.lab2.function.AbstractFunction;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Sine extends AbstractFunction {

    public Sine() {
        super();
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) throws ArithmeticException {
        isValid(x, precision);

        MathContext mc = new MathContext(precision.scale() + 10, RoundingMode.HALF_EVEN);

        BigDecimal pi = BigDecimalMath.pi(mc);
        BigDecimal tau = pi.multiply(BigDecimal.valueOf(2));
        x = x.remainder(tau);

        if (x.compareTo(pi) > 0) {
            x = x.subtract(tau);
        } else if (x.compareTo(pi.negate()) < 0) {
            x = x.add(tau);
        }

        BigDecimal result = x;
        BigDecimal term = x;
        BigDecimal x2 = x.multiply(x, mc);

        int i = 1;
        do {
            term = term.multiply(x2, mc)
                    .divide(BigDecimal.valueOf((2L * i) * (2L * i + 1)), mc);
            result = result.add(term.multiply(minusOnePower(i)), mc);
            i++;
        } while (term.abs().compareTo(precision.divide(BigDecimal.TEN, mc)) > 0);

        return result.setScale(precision.scale(), RoundingMode.HALF_EVEN);
    }

    private static BigDecimal minusOnePower(int n) {
        return BigDecimal.valueOf(1L - (n % 2) * 2);
    }
}
