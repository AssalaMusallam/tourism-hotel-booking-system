package com.swer313.projectstep1.availabilitypricing.currency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private ExchangeRateRepository repository;

    @InjectMocks
    private CurrencyService service;

    @Test
    void convert_sameCurrency_returnsSame() {
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal out = service.convert(amount, "USD", "USD");
        assertEquals(amount, out);
    }

    @Test
    void convert_usesRepositoryRate_and_scales() {
        when(repository.findByFromCurrencyAndToCurrency("USD","ILS"))
                .thenReturn(Optional.of(new ExchangeRate() {{ setRate(new BigDecimal("3.67")); }}));

        BigDecimal out = service.convert(new BigDecimal("100.00"), "USD", "ILS");
        assertEquals(new BigDecimal("367.00"), out);
    }

    @Test
    void convert_missingRate_throws() {
        when(repository.findByFromCurrencyAndToCurrency(anyString(), anyString()))
                .thenReturn(Optional.empty());

        assertThrows(CurrencyNotFoundException.class, () -> service.convert(new BigDecimal("1"), "USD", "XYZ"));
    }

    @Test
    void getSupportedCurrencies_delegates() {
        when(repository.findByFromCurrencyOrderByToCurrencyAsc("USD")).thenReturn(List.of(new ExchangeRate()));
        var list = service.getSupportedCurrencies();
        assertNotNull(list);
    }
}

