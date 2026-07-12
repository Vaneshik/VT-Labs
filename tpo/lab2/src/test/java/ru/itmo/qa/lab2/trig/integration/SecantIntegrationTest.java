package ru.itmo.qa.lab2.trig.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.qa.lab2.stub.CosineStub;
import ru.itmo.qa.lab2.stub.SineStub;
import ru.itmo.qa.lab2.trig.Cosine;
import ru.itmo.qa.lab2.trig.Secant;
import ru.itmo.qa.lab2.trig.Sine;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_EVEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecantIntegrationTest {

    private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

    @Mock
    private Cosine mockCos;

    @Mock
    private Sine mockSin;

    @Test
    void shouldCalculateWithCosineStub() {
        Secant sec = new Secant(new CosineStub());
        BigDecimal result = sec.calculate(BigDecimal.ZERO, PRECISION);
        assertNotNull(result);
    }

    @Test
    void shouldCalculateWithRealCosineAndSineStub() {
        Secant sec = new Secant(new Cosine(new SineStub()));
        BigDecimal result = sec.calculate(BigDecimal.ZERO, PRECISION);
        assertNotNull(result);
    }

    @Test
    void shouldCallSineThroughRealCosine() {
        when(mockSin.calculate(any(), any())).thenReturn(BigDecimal.valueOf(0.5));
        Cosine cosWithMockSin = new Cosine(mockSin);
        Secant sec = new Secant(cosWithMockSin);
        sec.calculate(new BigDecimal("1"), PRECISION);
        verify(mockSin, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    void shouldCallCosineFunction() {
        when(mockCos.calculate(any(BigDecimal.class), any(BigDecimal.class))).thenReturn(BigDecimal.ONE);
        Secant sec = new Secant(mockCos);
        sec.calculate(new BigDecimal("986"), PRECISION);
        verify(mockCos, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    }

    @ParameterizedTest(name = "mock.sec({0}) = {1}")
    @CsvFileSource(resources = "/integration/secIT.csv", numLinesToSkip = 1, delimiter = ',')
    void shouldCalculateWithMockCosine(BigDecimal x, BigDecimal y) {
        when(mockCos.calculate(eq(x), any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(Math.cos(x.doubleValue())));
        Secant sec = new Secant(mockCos);
        assertEquals(y, sec.calculate(x, PRECISION));
    }
}
