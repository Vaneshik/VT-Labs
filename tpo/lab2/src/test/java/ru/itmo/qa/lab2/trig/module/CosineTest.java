package ru.itmo.qa.lab2.trig.module;

import ch.obermuhlner.math.big.BigDecimalMath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import ru.itmo.qa.lab2.trig.Cosine;
import ru.itmo.qa.lab2.trig.Sine;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.MathContext.DECIMAL128;
import static java.math.RoundingMode.HALF_EVEN;
import static org.junit.jupiter.api.Assertions.*;

class CosineTest {

    private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

    private Cosine cos;

    @BeforeEach
    void init() {
        cos = new Cosine(new Sine());
    }

    @Test
    void shouldCalculateMaximum() {
        assertEquals(ONE.setScale(7, HALF_EVEN), cos.calculate(ZERO, PRECISION));
    }

    @Test
    void shouldCalculateMinimum() {
        MathContext mc = new MathContext(7, HALF_EVEN);
        BigDecimal pi = BigDecimalMath.pi(mc);
        assertAll(
            () -> assertEquals(ONE.negate().setScale(7, HALF_EVEN), cos.calculate(pi, PRECISION)),
            () -> assertEquals(ONE.negate().setScale(7, HALF_EVEN), cos.calculate(pi.negate(), PRECISION))
        );
    }

    @Test
    void shouldCalculateForPiHalf() {
        MathContext mc = new MathContext(DECIMAL128.getPrecision());
        BigDecimal piHalf = BigDecimalMath.pi(mc).divide(BigDecimal.valueOf(2), DECIMAL128.getPrecision(), HALF_EVEN);
        assertEquals(ZERO.setScale(7, HALF_EVEN), cos.calculate(piHalf, PRECISION));
        assertEquals(ZERO.setScale(7, HALF_EVEN), cos.calculate(piHalf.negate(), PRECISION));
    }

    @ParameterizedTest(name = "cos({0}) = {1}")
    @CsvFileSource(resources = "/cos.csv", numLinesToSkip = 1, delimiter = ',')
    void testCos(BigDecimal x, BigDecimal y) {
        assertEquals(y, cos.calculate(x, PRECISION));
    }
}
