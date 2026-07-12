package ru.itmo.qa.lab2.trig.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.qa.lab2.stub.SineStub;
import ru.itmo.qa.lab2.trig.Cosine;
import ru.itmo.qa.lab2.trig.Sine;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_EVEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CosineIntegrationTest {

    private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

    @Mock
    private Sine mockSin;

    @Test
    void shouldCalculateWithSineStub() {
        Cosine cos = new Cosine(new SineStub());
        BigDecimal result = cos.calculate(BigDecimal.ZERO, PRECISION);
        assertNotNull(result);
    }

    @Test
    void shouldCallSineFunction() {
        when(mockSin.calculate(any(BigDecimal.class), any(BigDecimal.class))).thenReturn(BigDecimal.ONE);
        Cosine cos = new Cosine(mockSin);
        cos.calculate(new BigDecimal("1"), PRECISION);
        verify(mockSin, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    }

    @ParameterizedTest(name = "mock.cos({0}) = {1}")
    @CsvFileSource(resources = "/integration/cosIT.csv", numLinesToSkip = 1, delimiter = ',')
    void shouldCalculateWithMockSine(BigDecimal x, BigDecimal expected) {
        when(mockSin.calculate(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(expected);
        Cosine cos = new Cosine(mockSin);
        assertEquals(expected, cos.calculate(x, PRECISION));
    }
}
