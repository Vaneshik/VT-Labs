package ru.itmo.qa.lab2.function;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
public abstract class AbstractFunction implements FunctionRule {
    private static final int MAX_ITERATIONS = 1000;

    protected final int seriesLength;

    protected AbstractFunction() {
        this.seriesLength = MAX_ITERATIONS;
    }

    protected void isValid(final BigDecimal x, final BigDecimal precision) {
        Objects.requireNonNull(x, "Аргумент не должен быть null");
        Objects.requireNonNull(precision, "Точность не должна быть null");
        if (precision.compareTo(BigDecimal.ZERO) <= 0 || precision.compareTo(BigDecimal.ONE) >= 0) {
            throw new ArithmeticException("Значение точности должно быть между 0 и 1 включительно");
        }
    }
}
