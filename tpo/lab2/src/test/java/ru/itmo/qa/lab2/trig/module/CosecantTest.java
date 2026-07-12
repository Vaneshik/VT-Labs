package ru.itmo.qa.lab2.trig.module;

import ch.obermuhlner.math.big.BigDecimalMath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.itmo.qa.lab2.trig.Cosecant;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.lang.String.format;
import static java.math.BigDecimal.ONE;
import static java.math.RoundingMode.HALF_EVEN;
import static org.junit.jupiter.api.Assertions.*;

class CosecantTest {

    private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

    private Cosecant csc;

    @BeforeEach
    void init() {
        csc = new Cosecant();
    }

    @Test
    void shouldCalculateMinimum() {
        MathContext mc = new MathContext(PRECISION.scale(), HALF_EVEN);
        BigDecimal arg = BigDecimalMath.pi(mc).divide(BigDecimal.valueOf(2), mc);
        assertEquals(ONE.setScale(PRECISION.scale(), HALF_EVEN), csc.calculate(arg, PRECISION));
    }

    @Test
    void shouldCalculateMaximum() {
        MathContext mc = new MathContext(PRECISION.scale(), HALF_EVEN);
        BigDecimal arg = BigDecimalMath.pi(mc).divide(BigDecimal.valueOf(2), mc).negate();
        assertEquals(ONE.negate().setScale(PRECISION.scale(), HALF_EVEN), csc.calculate(arg, PRECISION));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-Math.PI, 0, Math.PI})
    void shouldThrowAtSingularities(double x) {
        BigDecimal arg = BigDecimal.valueOf(x).setScale(PRECISION.scale(), HALF_EVEN);
        Throwable ex = assertThrows(ArithmeticException.class, () -> csc.calculate(arg, PRECISION));
        assertEquals(format("У косеканса нет значения при x = %s", arg), ex.getMessage());
    }

    @ParameterizedTest(name = "csc({0}) = {1}")
    @CsvFileSource(resources = "/csc.csv", numLinesToSkip = 1, delimiter = ',')
    void testCsc(BigDecimal x, BigDecimal y) {
        assertEquals(y, csc.calculate(x, PRECISION));
    }
}
