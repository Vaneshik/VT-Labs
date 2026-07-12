package ru.itmo.qa.lab2.trig.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.qa.lab2.stub.SineStub;
import ru.itmo.qa.lab2.trig.Cosecant;
import ru.itmo.qa.lab2.trig.Sine;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_EVEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CosecantIntegrationTest {

    private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

    @Mock
    private Sine mockSin;

    @Test
    void shouldCalculateWithSineStub() {
        Cosecant csc = new Cosecant(new SineStub());
        BigDecimal result = csc.calculate(new BigDecimal("1.5707963"), PRECISION);
        assertNotNull(result);
    }

    @Test
    void shouldCallSineFunction() {
        when(mockSin.calculate(any(BigDecimal.class), any(BigDecimal.class))).thenReturn(BigDecimal.ONE);
        Cosecant csc = new Cosecant(mockSin);
        csc.calculate(new BigDecimal("965"), PRECISION);
        verify(mockSin, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    }

    @ParameterizedTest(name = "mock.csc({0}) = {1}")
    @CsvFileSource(resources = "/integration/cscIT.csv", numLinesToSkip = 1, delimiter = ',')
    void shouldCalculateWithMockSine(BigDecimal x, BigDecimal y) {
        when(mockSin.calculate(eq(x), any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(Math.sin(x.doubleValue())));
        Cosecant csc = new Cosecant(mockSin);
        assertEquals(y, csc.calculate(x, PRECISION));
    }
}
