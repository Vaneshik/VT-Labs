package ru.itmo.qa.lab2.function;

import java.math.BigDecimal;

public interface FunctionRule {
    BigDecimal calculate(final BigDecimal x, final BigDecimal precision);
}
