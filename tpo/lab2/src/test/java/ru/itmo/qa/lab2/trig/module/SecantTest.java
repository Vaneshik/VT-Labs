package ru.itmo.qa.lab2.trig.module;

import ch.obermuhlner.math.big.BigDecimalMath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import ru.itmo.qa.lab2.trig.Secant;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.DECIMAL128;
import static java.math.RoundingMode.HALF_EVEN;
import static org.junit.jupiter.api.Assertions.*;

class SecantTest {

    private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

    private Secant sec;

    @BeforeEach
    void init() {
        sec = new Secant();
    }

    @Test
    void shouldCalculateMinimum() {
        assertEquals(ONE.setScale(7, HALF_EVEN), sec.calculate(ZERO, PRECISION));
    }

    @Test
    void shouldCalculateMaximum() {
        MathContext mc = new MathContext(7, HALF_EVEN);
        BigDecimal pi = BigDecimalMath.pi(mc);
        assertAll(
            () -> assertEquals(ONE.negate().setScale(7, HALF_EVEN), sec.calculate(pi, PRECISION)),
            () -> assertEquals(ONE.negate().setScale(7, HALF_EVEN), sec.calculate(pi.negate(), PRECISION))
        );
    }

    @Test
    void shouldThrowAtPiHalf() {
        MathContext mc = new MathContext(DECIMAL128.getPrecision(), HALF_EVEN);
        BigDecimal piHalf = BigDecimalMath.pi(mc).divide(BigDecimal.valueOf(2), mc);
        BigDecimal negPiHalf = piHalf.negate();
        assertAll(
            () -> assertThrows(ArithmeticException.class, () -> sec.calculate(piHalf, PRECISION)),
            () -> assertThrows(ArithmeticException.class, () -> sec.calculate(negPiHalf, PRECISION))
        );
    }

    @ParameterizedTest(name = "sec({0}) = {1}")
    @CsvFileSource(resources = "/sec.csv", numLinesToSkip = 1, delimiter = ',')
    void testSec(BigDecimal x, BigDecimal y) {
        assertEquals(y, sec.calculate(x, PRECISION));
    }
}
