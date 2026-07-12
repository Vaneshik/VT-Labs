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
import ru.itmo.qa.lab2.trig.Sine;
import ru.itmo.qa.lab2.trig.Tangent;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TangentIntegrationTest {

    private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

    @Mock
    private Sine mockSin;
    @Mock
    private Cosine mockCos;

    @Test
    void shouldCalculateWithStubs() {
        Tangent tan = new Tangent(new SineStub(), new CosineStub());
        BigDecimal result = tan.calculate(BigDecimal.ZERO, PRECISION);
        assertNotNull(result);
    }

    @Test
    void shouldCalculateWithRealSineAndMockCosine() {
        when(mockCos.calculate(any(), any())).thenReturn(BigDecimal.valueOf(0.5));
        Tangent tan = new Tangent(new Sine(), mockCos);
        BigDecimal result = tan.calculate(new BigDecimal("1"), PRECISION);
        assertNotNull(result);
        verify(mockCos, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    void shouldCallSineAndCosine() {
        when(mockSin.calculate(any(BigDecimal.class), any(BigDecimal.class))).thenReturn(BigDecimal.ONE);
        when(mockCos.calculate(any(BigDecimal.class), any(BigDecimal.class))).thenReturn(BigDecimal.ONE);
        Tangent tan = new Tangent(mockSin, mockCos);
        tan.calculate(new BigDecimal("972"), PRECISION);
        verify(mockSin, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
        verify(mockCos, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    }

    @ParameterizedTest(name = "mock.tan({0}) = {1}")
    @CsvFileSource(resources = "/integration/tanIT.csv", numLinesToSkip = 1, delimiter = ',')
    void shouldCalculateWithMocks(BigDecimal x, BigDecimal y) {
        when(mockSin.calculate(eq(x), any())).thenReturn(new BigDecimal(Math.sin(x.doubleValue())));
        when(mockCos.calculate(eq(x), any())).thenReturn(new BigDecimal(Math.cos(x.doubleValue())));
        Tangent tan = new Tangent(mockSin, mockCos);
        assertEquals(y, tan.calculate(x, PRECISION));
    }

    @Test
    void shouldThrowWhenCosIsZero() {
        BigDecimal x = BigDecimal.valueOf(Math.PI / 2);
        when(mockSin.calculate(eq(x), any())).thenReturn(BigDecimal.ONE);
        when(mockCos.calculate(eq(x), any())).thenReturn(BigDecimal.ZERO);
        Tangent tan = new Tangent(mockSin, mockCos);
        assertThrows(ArithmeticException.class, () -> tan.calculate(x, PRECISION));
    }
}
