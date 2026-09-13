package com.example.recipeplatform.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    private LoggingAspect loggingAspect;

    @BeforeEach
    void setUp() {
        loggingAspect = new LoggingAspect();
        when(joinPoint.getTarget()).thenReturn(new DummyService());
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("doSomething");
    }

    @Test
    @DisplayName("Should proceed and return result when method succeeds")
    void shouldLogSuccessAndReturnResult() throws Throwable {
        when(joinPoint.proceed()).thenReturn("success-result");

        Object result = loggingAspect.logExecutionTime(joinPoint);

        assertThat(result).isEqualTo("success-result");
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("Should propagate exception when target method fails")
    void shouldPropagateExceptionWhenMethodFails() throws Throwable {
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("Service failed"));

        assertThatThrownBy(() -> loggingAspect.logExecutionTime(joinPoint))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Service failed");

        verify(joinPoint).proceed();
    }

    static class DummyService {
    }
}
