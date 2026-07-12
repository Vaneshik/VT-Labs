package ru.itmo.qa.lab2;

import lombok.AllArgsConstructor;
import ru.itmo.qa.lab2.function.AbstractFunction;
import ru.itmo.qa.lab2.log.BaseNLogarithm;
import ru.itmo.qa.lab2.log.NaturalLogarithm;
import ru.itmo.qa.lab2.trig.*;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.DECIMAL128;
import static java.math.RoundingMode.HALF_EVEN;

@AllArgsConstructor
public class EquationSystem extends AbstractFunction {

    private final Sine sin;
    private final Cosine cos;
    private final Secant sec;
    private final Cosecant csc;
    private final Tangent tan;
    private final Cotangent cot;

    private final NaturalLogarithm ln;
    private final BaseNLogarithm log3;
    private final BaseNLogarithm log5;
    private final BaseNLogarithm log10;

    public EquationSystem() {
        super();
        sin = new Sine();
        cos = new Cosine();
        sec = new Secant();
        csc = new Cosecant();
        tan = new Tangent();
        cot = new Cotangent();

        ln = new NaturalLogarithm();
        log3 = new BaseNLogarithm(3);
        log5 = new BaseNLogarithm(5);
        log10 = new BaseNLogarithm(10);
    }

    @Override
    public BigDecimal calculate(BigDecimal x, BigDecimal precision) {
        isValid(x, precision);

        final MathContext mc = new MathContext(DECIMAL128.getPrecision(), HALF_EVEN);
        final BigDecimal p = precision.setScale(precision.scale() + 10, HALF_EVEN);

        if (x.compareTo(ZERO) <= 0) {
            return calcTrig(x, p, mc, precision);
        } else {
            return calcLog(x, p, mc, precision);
        }
    }

    private BigDecimal calcTrig(BigDecimal x, BigDecimal p, MathContext mc, BigDecimal precision) {
        try {
            BigDecimal sinV = c(sin, x, p);
            BigDecimal cosV = c(cos, x, p);
            BigDecimal secV = c(sec, x, p);
            BigDecimal cscV = c(csc, x, p);
            BigDecimal tanV = c(tan, x, p);
            BigDecimal cotV = c(cot, x, p);

            BigDecimal inner = cscV.pow(3, mc).add(cosV).subtract(tanV).subtract(tanV);
            BigDecimal big = inner.pow(2, mc).pow(3, mc).pow(2, mc);
            BigDecimal leftFactor = big.multiply(sinV.add(tanV).multiply(secV, mc), mc);

            BigDecimal a = cscV.multiply(secV, mc).add(tanV).subtract(sinV.pow(3, mc)).pow(2, mc);
            BigDecimal b = cscV.add(cotV.pow(3, mc)).pow(2, mc).pow(2, mc);
            BigDecimal rightFactor = a.add(b);

            BigDecimal mainPart = leftFactor.multiply(rightFactor, mc);

            BigDecimal addend = sinV.multiply(tanV, mc)
                    .subtract(cscV.add(secV).multiply(cscV, mc).divide(secV, mc));

            return mainPart.add(addend).setScale(precision.scale(), HALF_EVEN);
        } catch (ArithmeticException e) {
            throw new ArithmeticException(format("У функции нет значения при x = %s", x));
        }
    }

    private BigDecimal calcLog(BigDecimal x, BigDecimal p, MathContext mc, BigDecimal precision) {
        try {
            BigDecimal l10 = c(log10, x, p);
            BigDecimal l3 = c(log3, x, p);
            BigDecimal l5 = c(log5, x, p);

            if (l10.compareTo(ZERO) == 0) {
                throw new ArithmeticException(format("У функции нет значения при x = %s", x));
            }
            if (l3.compareTo(ZERO) == 0 || l5.compareTo(ZERO) == 0) {
                throw new ArithmeticException(format("У функции нет значения при x = %s", x));
            }

            BigDecimal ratio = l10.divide(l10, mc);
            BigDecimal num = ratio.pow(3, mc).multiply(l10, mc);
            BigDecimal den = l3.multiply(l10.multiply(l5, mc), mc);

            return num.divide(den, mc).pow(2, mc).setScale(precision.scale(), HALF_EVEN);
        } catch (ArithmeticException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("У функции")) {
                throw e;
            }
            throw new ArithmeticException(format("У функции нет значения при x = %s", x));
        }
    }

    private BigDecimal c(AbstractFunction function, BigDecimal x, BigDecimal precision) {
        return function.calculate(x, precision);
    }
}
