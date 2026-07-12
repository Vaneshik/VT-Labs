package ru.itmo.qa.lab2.function.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.qa.lab2.EquationSystem;
import ru.itmo.qa.lab2.log.BaseNLogarithm;
import ru.itmo.qa.lab2.log.NaturalLogarithm;
import ru.itmo.qa.lab2.trig.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquationSystemIntegrationTest {

    private static final BigDecimal PRECISION = new BigDecimal("0.0000001");

    @Mock private Sine mockSin;
    @Mock private Cosine mockCos;
    @Mock private Secant mockSec;
    @Mock private Cosecant mockCsc;
    @Mock private Tangent mockTan;
    @Mock private Cotangent mockCot;
    @Mock private NaturalLogarithm mockLn;
    @Mock private BaseNLogarithm mockLog3;
    @Mock private BaseNLogarithm mockLog5;
    @Mock private BaseNLogarithm mockLog10;

    @Test
    void shouldCallAllTrigFunctions() {
        when(mockSin.calculate(any(), any())).thenReturn(BigDecimal.ONE);
        when(mockCos.calculate(any(), any())).thenReturn(BigDecimal.ONE);
        when(mockSec.calculate(any(), any())).thenReturn(BigDecimal.ONE);
        when(mockCsc.calculate(any(), any())).thenReturn(BigDecimal.ONE);
        when(mockTan.calculate(any(), any())).thenReturn(BigDecimal.ONE);
        when(mockCot.calculate(any(), any())).thenReturn(BigDecimal.ONE);

        EquationSystem system = new EquationSystem(
                mockSin, mockCos, mockSec, mockCsc, mockTan, mockCot,
                mockLn, mockLog3, mockLog5, mockLog10);
        system.calculate(new BigDecimal("-1"), new BigDecimal("0.0001"));
        verify(mockSin, atLeastOnce()).calculate(any(), any());
        verify(mockCos, atLeastOnce()).calculate(any(), any());
        verify(mockSec, atLeastOnce()).calculate(any(), any());
        verify(mockCsc, atLeastOnce()).calculate(any(), any());
        verify(mockTan, atLeastOnce()).calculate(any(), any());
        verify(mockCot, atLeastOnce()).calculate(any(), any());
        verifyNoInteractions(mockLn);
        verifyNoInteractions(mockLog3);
        verifyNoInteractions(mockLog5);
        verifyNoInteractions(mockLog10);
    }

    @Test
    void shouldCalculateTrigWithAllRealAndMockedLogs() {
        EquationSystem system = new EquationSystem(
                new Sine(), new Cosine(), new Secant(), new Cosecant(), new Tangent(), new Cotangent(),
                mockLn, mockLog3, mockLog5, mockLog10);
        BigDecimal result = system.calculate(new BigDecimal("-1"), new BigDecimal("0.0001"));
        assertNotNull(result);
        verifyNoInteractions(mockLn);
        verifyNoInteractions(mockLog3);
        verifyNoInteractions(mockLog5);
        verifyNoInteractions(mockLog10);
    }

    @Test
    void shouldCallAllLogFunctions() {
        when(mockLog10.calculate(any(), any())).thenReturn(new BigDecimal("2"));
        when(mockLog3.calculate(any(), any())).thenReturn(new BigDecimal("3"));
        when(mockLog5.calculate(any(), any())).thenReturn(new BigDecimal("4"));

        EquationSystem system = new EquationSystem(
                mockSin, mockCos, mockSec, mockCsc, mockTan, mockCot,
                mockLn, mockLog3, mockLog5, mockLog10);
        system.calculate(new BigDecimal("5"), new BigDecimal("0.0001"));
        verify(mockLn, never()).calculate(any(), any());
        verify(mockLog3, atLeastOnce()).calculate(any(), any());
        verify(mockLog5, atLeastOnce()).calculate(any(), any());
        verify(mockLog10, atLeastOnce()).calculate(any(), any());
        verifyNoInteractions(mockSin);
        verifyNoInteractions(mockCos);
        verifyNoInteractions(mockSec);
        verifyNoInteractions(mockCsc);
        verifyNoInteractions(mockTan);
        verifyNoInteractions(mockCot);
    }

    @Test
    void shouldCalculateCorrectlyWithMockedLogs() {
        when(mockLog10.calculate(any(), any())).thenReturn(new BigDecimal("2"));
        when(mockLog3.calculate(any(), any())).thenReturn(new BigDecimal("3"));
        when(mockLog5.calculate(any(), any())).thenReturn(new BigDecimal("4"));

        EquationSystem system = new EquationSystem(
                mockSin, mockCos, mockSec, mockCsc, mockTan, mockCot,
                mockLn, mockLog3, mockLog5, mockLog10);

        BigDecimal result = system.calculate(new BigDecimal("42"), PRECISION);
        BigDecimal expected = new BigDecimal("0.0069444");
        assertEquals(expected, result);
    }

    @Test
    void shouldCalculateLogWithAllRealAndMockedTrig() {
        EquationSystem system = new EquationSystem(
                mockSin, mockCos, mockSec, mockCsc, mockTan, mockCot,
                mockLn, new BaseNLogarithm(3), new BaseNLogarithm(5), new BaseNLogarithm(10));
        BigDecimal result = system.calculate(new BigDecimal("5"), new BigDecimal("0.0001"));
        assertNotNull(result);
        verifyNoInteractions(mockSin);
        verifyNoInteractions(mockCos);
    }

    @Test
    void shouldCalculateFullSystemTrigBranch() {
        EquationSystem system = new EquationSystem();
        BigDecimal result = system.calculate(new BigDecimal("-1"), new BigDecimal("0.0001"));
        assertNotNull(result);
    }

    @Test
    void shouldCalculateFullSystemLogBranch() {
        EquationSystem system = new EquationSystem();
        BigDecimal result = system.calculate(new BigDecimal("5"), new BigDecimal("0.0001"));
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void shouldThrowWhenTrigThrows() {
        when(mockCsc.calculate(any(), any())).thenThrow(new ArithmeticException("no value"));
        EquationSystem system = new EquationSystem(
                mockSin, mockCos, mockSec, mockCsc, mockTan, mockCot,
                mockLn, mockLog3, mockLog5, mockLog10);
        assertThrows(ArithmeticException.class, () -> system.calculate(new BigDecimal("-1"), PRECISION));
    }
}
