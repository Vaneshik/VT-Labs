package ru.itmo.qa.lab2.trig.module;

import ch.obermuhlner.math.big.BigDecimalMath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import ru.itmo.qa.lab2.trig.Sine;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.DECIMAL128;
import static java.math.RoundingMode.HALF_EVEN;
import static org.junit.jupiter.api.Assertions.*;

class SineTest {

    private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

    private Sine sin;

    @BeforeEach
    void init() {
        sin = new Sine();
    }

    @Test
    void shouldCalculateForZero() {
        assertEquals(ZERO.setScale(7, HALF_EVEN), sin.calculate(ZERO, PRECISION));
    }

    @Test
    void shouldCalculateMaximum() {
        MathContext mc = new MathContext(DECIMAL128.getPrecision());
        BigDecimal piHalf = BigDecimalMath.pi(mc).divide(BigDecimal.valueOf(2), DECIMAL128.getPrecision(), HALF_EVEN);
        assertEquals(ONE.setScale(7, HALF_EVEN), sin.calculate(piHalf, PRECISION));
    }

    @Test
    void shouldCalculateMinimum() {
        MathContext mc = new MathContext(DECIMAL128.getPrecision());
        BigDecimal negPiHalf = BigDecimalMath.pi(mc).divide(BigDecimal.valueOf(2), DECIMAL128.getPrecision(), HALF_EVEN).negate();
        assertEquals(ONE.negate().setScale(7, HALF_EVEN), sin.calculate(negPiHalf, PRECISION));
    }

    @Test
    void shouldCalculateForPi() {
        MathContext mc = new MathContext(DECIMAL128.getPrecision());
        BigDecimal pi = BigDecimalMath.pi(mc);
        assertEquals(ZERO.setScale(7, HALF_EVEN), sin.calculate(pi, PRECISION));
        assertEquals(ZERO.setScale(7, HALF_EVEN), sin.calculate(pi.negate(), PRECISION));
    }

    @Test
    void shouldNotAcceptNull() {
        assertThrows(NullPointerException.class, () -> sin.calculate(null, PRECISION));
        assertThrows(NullPointerException.class, () -> sin.calculate(ZERO, null));
    }

    @Test
    void shouldNotAcceptInvalidPrecision() {
        assertThrows(ArithmeticException.class, () -> sin.calculate(ZERO, BigDecimal.ZERO));
        assertThrows(ArithmeticException.class, () -> sin.calculate(ZERO, BigDecimal.ONE));
        assertThrows(ArithmeticException.class, () -> sin.calculate(ZERO, new BigDecimal("-0.1")));
    }

    @ParameterizedTest(name = "sin({0}) = {1}")
    @CsvFileSource(resources = "/sin.csv", numLinesToSkip = 1, delimiter = ',')
    void testSin(BigDecimal x, BigDecimal y) {
        assertEquals(y, sin.calculate(x, PRECISION));
    }
}
