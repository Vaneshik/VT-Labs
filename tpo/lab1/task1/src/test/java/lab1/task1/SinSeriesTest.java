package lab1.task1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SinSeriesTest {

    private static final double EPS = 1e-9;
    private static final double EPS_LOOSE = 1e-5;

    @ParameterizedTest(name = "sin({0}) = {1}")
    @CsvSource({
        "0.0,                  0.0",
        "0.5235987755982988,   0.5",
        "0.7853981633974483,   0.7071067811865476",
        "1.0471975511965976,   0.8660254037844387",
        "1.5707963267948966,   1.0",
        "2.0943951023931953,   0.8660254037844384",
        "2.617993877991494,    0.49999999999999994",
        "3.141592653589793,    0.0",
        "-0.5235987755982988, -0.5",
        "-1.5707963267948966, -1.0",
        "-3.141592653589793,   0.0"
    })
    void tableValues(double x, double expected) {
        assertEquals(expected, SinSeries.sin(x), EPS_LOOSE);
    }

    @Test
    void sinOfZeroIsZero() {
        assertEquals(0.0, SinSeries.sin(0.0), EPS);
    }

    @Test
    void sinOfPiHalfIsOne() {
        assertEquals(1.0, SinSeries.sin(Math.PI / 2), EPS_LOOSE);
    }

    @Test
    void sinOfPiIsZero() {
        assertEquals(0.0, SinSeries.sin(Math.PI), EPS_LOOSE);
    }

    @Test
    void sinOfMinusPiHalfIsMinusOne() {
        assertEquals(-1.0, SinSeries.sin(-Math.PI / 2), EPS_LOOSE);
    }

    @Test
    void sinOfTwoPiIsZero() {
        assertEquals(0.0, SinSeries.sin(2 * Math.PI), EPS_LOOSE);
    }

    @Test
    void sinOfThreePiHalfIsMinusOne() {
        assertEquals(-1.0, SinSeries.sin(3 * Math.PI / 2), EPS_LOOSE);
    }

    @Test
    void sinOfNaNIsNaN() {
        assertTrue(Double.isNaN(SinSeries.sin(Double.NaN)));
    }

    @Test
    void sinOfPositiveInfinityIsNaN() {
        assertTrue(Double.isNaN(SinSeries.sin(Double.POSITIVE_INFINITY)));
    }

    @Test
    void sinOfNegativeInfinityIsNaN() {
        assertTrue(Double.isNaN(SinSeries.sin(Double.NEGATIVE_INFINITY)));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.1, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0})
    void sinIsOddFunction(double x) {
        assertEquals(SinSeries.sin(-x), -SinSeries.sin(x), EPS_LOOSE);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 0.3, 1.0, 1.57, 2.5, 3.1})
    void sinIsPeriodic(double x) {
        assertEquals(SinSeries.sin(x), SinSeries.sin(x + 2 * Math.PI), EPS_LOOSE);
        assertEquals(SinSeries.sin(x), SinSeries.sin(x - 2 * Math.PI), EPS_LOOSE);
    }

    @ParameterizedTest
    @ValueSource(doubles = {10.0, 100.0, -50.0, 37.5})
    void sinLargeArgumentsMatchMath(double x) {
        assertEquals(Math.sin(x), SinSeries.sin(x), EPS_LOOSE);
    }

    @Test
    void moreTermsGiveBetterPrecision() {
        double x = 1.2;
        double reference = Math.sin(x);
        double err5 = Math.abs(SinSeries.sin(x, 5) - reference);
        double err15 = Math.abs(SinSeries.sin(x, 15) - reference);
        assertTrue(err15 < err5);
    }

    @Test
    void invalidTermCountThrows() {
        assertThrows(IllegalArgumentException.class, () -> SinSeries.sin(1.0, 0));
        assertThrows(IllegalArgumentException.class, () -> SinSeries.sin(1.0, -1));
    }

    // fuzzing

    @Test
    void randomValuesMatchMathSin() {
        Random rng = new Random(42);
        for (int i = 0; i < 10000; i++) {
            double x = (rng.nextDouble() - 0.5) * 40 * Math.PI;
            assertEquals(Math.sin(x), SinSeries.sin(x), EPS_LOOSE,
                    "Mismatch at x = " + x);
        }
    }
}
