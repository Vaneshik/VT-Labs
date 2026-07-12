package ru.itmo.qa.lab2.trig.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.itmo.qa.lab2.trig.Tangent;

import java.math.BigDecimal;

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static org.junit.jupiter.api.Assertions.*;

class TangentTest {

    private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

    private Tangent tan;

    @BeforeEach
    void init() {
        tan = new Tangent();
    }

    @ParameterizedTest(name = "tan({0}) = 0")
    @ValueSource(doubles = {-Math.PI, 0, Math.PI})
    void shouldCalculateZeroCrossings(double x) {
        assertEquals(ZERO.setScale(PRECISION.scale(), HALF_EVEN),
                tan.calculate(BigDecimal.valueOf(x), PRECISION));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-Math.PI / 2, Math.PI / 2})
    void shouldThrowAtSingularities(double x) {
        BigDecimal arg = BigDecimal.valueOf(x).setScale(PRECISION.scale(), HALF_EVEN);
        Throwable ex = assertThrows(ArithmeticException.class, () -> tan.calculate(arg, PRECISION));
        assertEquals(format("У тангенса нет значения при x = %s", arg), ex.getMessage());
    }

    @ParameterizedTest(name = "tan({0}) = {1}")
    @CsvFileSource(resources = "/tan.csv", numLinesToSkip = 1, delimiter = ',')
    void testTan(BigDecimal x, BigDecimal y) {
        assertEquals(y, tan.calculate(x, PRECISION));
    }
}
