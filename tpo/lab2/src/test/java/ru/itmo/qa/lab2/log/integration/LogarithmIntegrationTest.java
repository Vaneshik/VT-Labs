package ru.itmo.qa.lab2.log.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.qa.lab2.log.BaseNLogarithm;
import ru.itmo.qa.lab2.log.NaturalLogarithm;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogarithmIntegrationTest {

    private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

    @Mock
    private NaturalLogarithm mockLn;

    @Test
    void shouldCallNaturalLogarithm() {
        when(mockLn.calculate(any(BigDecimal.class), any(BigDecimal.class))).thenReturn(BigDecimal.ONE);
        BaseNLogarithm log3 = new BaseNLogarithm(3, mockLn);
        log3.calculate(new BigDecimal("993"), new BigDecimal("0.001"));
        verify(mockLn, atLeastOnce()).calculate(any(BigDecimal.class), any(BigDecimal.class));
    }

    @ParameterizedTest(name = "mock.log3({0}) = {1}")
    @CsvFileSource(resources = "/integration/log3IT.csv", numLinesToSkip = 1, delimiter = ',')
    void shouldCalculateLog3WithMock(BigDecimal x, BigDecimal y) {
        when(mockLn.calculate(eq(x), any()))
                .thenReturn(BigDecimal.valueOf(Math.log(x.doubleValue())));
        when(mockLn.calculate(eq(new BigDecimal(3)), any()))
                .thenReturn(BigDecimal.valueOf(Math.log(3)));
        BaseNLogarithm log3 = new BaseNLogarithm(3, mockLn);
        assertEquals(y, log3.calculate(x, PRECISION));
    }

    @ParameterizedTest(name = "mock.log5({0}) = {1}")
    @CsvFileSource(resources = "/integration/log5IT.csv", numLinesToSkip = 1, delimiter = ',')
    void shouldCalculateLog5WithMock(BigDecimal x, BigDecimal y) {
        when(mockLn.calculate(eq(x), any()))
                .thenReturn(BigDecimal.valueOf(Math.log(x.doubleValue())));
        when(mockLn.calculate(eq(new BigDecimal(5)), any()))
                .thenReturn(BigDecimal.valueOf(Math.log(5)));
        BaseNLogarithm log5 = new BaseNLogarithm(5, mockLn);
        assertEquals(y, log5.calculate(x, PRECISION));
    }

    @ParameterizedTest(name = "mock.log10({0}) = {1}")
    @CsvFileSource(resources = "/integration/log10IT.csv", numLinesToSkip = 1, delimiter = ',')
    void shouldCalculateLog10WithMock(BigDecimal x, BigDecimal y) {
        when(mockLn.calculate(eq(x), any()))
                .thenReturn(BigDecimal.valueOf(Math.log(x.doubleValue())));
        when(mockLn.calculate(eq(new BigDecimal(10)), any()))
                .thenReturn(BigDecimal.valueOf(Math.log(10)));
        BaseNLogarithm log10 = new BaseNLogarithm(10, mockLn);
        assertEquals(y, log10.calculate(x, PRECISION));
    }
}
