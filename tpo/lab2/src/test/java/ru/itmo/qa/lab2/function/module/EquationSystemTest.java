package ru.itmo.qa.lab2.function.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.itmo.qa.lab2.EquationSystem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class EquationSystemTest {

    private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

    private EquationSystem system;

    @BeforeEach
    void init() {
        system = new EquationSystem();
    }

    @Test
    void shouldNotAcceptNullArgument() {
        assertThrows(NullPointerException.class, () -> system.calculate(null, PRECISION));
    }

    @Test
    void shouldNotAcceptNullPrecision() {
        assertThrows(NullPointerException.class, () -> system.calculate(new BigDecimal(-2), null));
    }

    @ParameterizedTest
    @MethodSource("illegalPrecisions")
    void shouldNotAcceptInvalidPrecision(BigDecimal precision) {
        assertThrows(ArithmeticException.class, () -> system.calculate(new BigDecimal(-2), precision));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -Math.PI, -2 * Math.PI})
    void shouldThrowAtTrigSingularitiesX0(double x) {
        assertThrows(ArithmeticException.class, () -> system.calculate(BigDecimal.valueOf(x), PRECISION));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-Math.PI / 2, -3 * Math.PI / 2})
    void shouldThrowAtTrigSingularitiesXPiHalf(double x) {
        assertThrows(ArithmeticException.class, () -> system.calculate(BigDecimal.valueOf(x), PRECISION));
    }

    @Test
    void shouldNotAcceptXEquals1() {
        assertThrows(ArithmeticException.class, () -> system.calculate(BigDecimal.ONE, PRECISION));
    }

    @Test
    void shouldCalculateForPositiveX() {
        BigDecimal result = system.calculate(new BigDecimal("2"), PRECISION);
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0, "f(2) должно быть положительным");
    }

    @Test
    void shouldCalculateForPositiveX10() {
        BigDecimal result = system.calculate(new BigDecimal("10"), PRECISION);
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0, "f(10) должно быть положительным");
    }

    @Test
    void shouldCalculateForNegativeX() {
        BigDecimal result = assertDoesNotThrow(() -> system.calculate(new BigDecimal("-1"), PRECISION));
        assertNotNull(result);
    }

    @Test
    void shouldCalculateForNegativeX2() {
        BigDecimal result = assertDoesNotThrow(() -> system.calculate(new BigDecimal("-2"), PRECISION));
        assertNotNull(result);
    }

    private static Stream<Arguments> illegalPrecisions() {
        return Stream.of(
            Arguments.of(BigDecimal.ZERO),
            Arguments.of(BigDecimal.ONE),
            Arguments.of(new BigDecimal("1.01")),
            Arguments.of(new BigDecimal("-0.01"))
        );
    }
}
