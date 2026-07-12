package ru.itmo.qa.lab2.log.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.itmo.qa.lab2.log.NaturalLogarithm;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static org.junit.jupiter.api.Assertions.*;

class NaturalLogarithmTest {

    private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

    private NaturalLogarithm ln;

    @BeforeEach
    void init() {
        ln = new NaturalLogarithm();
    }

    @Test
    void shouldCalculateForOne() {
        assertEquals(ZERO, ln.calculate(ONE, PRECISION));
    }

    @Test
    void shouldNotCalculateForZero() {
        assertThrows(ArithmeticException.class, () -> ln.calculate(ZERO, PRECISION));
    }

    @Test
    void shouldNotCalculateForNegative() {
        assertThrows(ArithmeticException.class, () -> ln.calculate(new BigDecimal("-1"), PRECISION));
    }

    @ParameterizedTest(name = "ln({0}) = {1}")
    @ValueSource(doubles = {0.1, 0.5, 2.0, 5.0, 10.0})
    void testLn(double d) {
        BigDecimal expected = BigDecimal.valueOf(Math.log(d)).setScale(7, HALF_EVEN);
        assertEquals(expected, ln.calculate(BigDecimal.valueOf(d), PRECISION));
    }

    @ParameterizedTest(name = "ln({0}) = {1}")
    @CsvFileSource(resources = "/ln.csv", numLinesToSkip = 1, delimiter = ',')
    void testLnCsv(BigDecimal x, BigDecimal y) {
        assertEquals(y, ln.calculate(x, PRECISION));
    }
}
