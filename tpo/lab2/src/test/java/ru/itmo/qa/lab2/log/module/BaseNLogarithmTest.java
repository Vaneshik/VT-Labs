package ru.itmo.qa.lab2.log.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import ru.itmo.qa.lab2.log.BaseNLogarithm;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static org.junit.jupiter.api.Assertions.*;

class BaseNLogarithmTest {

    private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

    private BaseNLogarithm log3;
    private BaseNLogarithm log5;
    private BaseNLogarithm log10;

    @BeforeEach
    void init() {
        log3 = new BaseNLogarithm(3);
        log5 = new BaseNLogarithm(5);
        log10 = new BaseNLogarithm(10);
    }

    @Test
    void shouldCalculateForOne() {
        assertAll(
            () -> assertEquals(ZERO.setScale(7, HALF_EVEN), log3.calculate(ONE, PRECISION)),
            () -> assertEquals(ZERO.setScale(7, HALF_EVEN), log5.calculate(ONE, PRECISION)),
            () -> assertEquals(ZERO.setScale(7, HALF_EVEN), log10.calculate(ONE, PRECISION))
        );
    }

    @Test
    void shouldCalculateForBase() {
        assertAll(
            () -> assertEquals(ONE.setScale(7, HALF_EVEN), log3.calculate(new BigDecimal("3"), PRECISION)),
            () -> assertEquals(ONE.setScale(7, HALF_EVEN), log5.calculate(new BigDecimal("5"), PRECISION)),
            () -> assertEquals(ONE.setScale(7, HALF_EVEN), log10.calculate(new BigDecimal("10"), PRECISION))
        );
    }

    @Test
    void shouldNotCalculateForZeroOrNegative() {
        assertThrows(ArithmeticException.class, () -> log3.calculate(ZERO, PRECISION));
        assertThrows(ArithmeticException.class, () -> log5.calculate(new BigDecimal("-1"), PRECISION));
    }

    @ParameterizedTest(name = "log3({0}) = {1}")
    @CsvFileSource(resources = "/log3.csv", numLinesToSkip = 1, delimiter = ',')
    void testLog3(BigDecimal x, BigDecimal y) {
        assertEquals(y, log3.calculate(x, PRECISION));
    }

    @ParameterizedTest(name = "log5({0}) = {1}")
    @CsvFileSource(resources = "/log5.csv", numLinesToSkip = 1, delimiter = ',')
    void testLog5(BigDecimal x, BigDecimal y) {
        assertEquals(y, log5.calculate(x, PRECISION));
    }

    @ParameterizedTest(name = "log10({0}) = {1}")
    @CsvFileSource(resources = "/log10.csv", numLinesToSkip = 1, delimiter = ',')
    void testLog10(BigDecimal x, BigDecimal y) {
        assertEquals(y, log10.calculate(x, PRECISION));
    }
}
