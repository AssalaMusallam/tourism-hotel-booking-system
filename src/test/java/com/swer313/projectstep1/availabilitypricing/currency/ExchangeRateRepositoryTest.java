package com.swer313.projectstep1.availabilitypricing.currency;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExchangeRateRepositoryTest {

    @Autowired
    private ExchangeRateRepository repository;

    @Test
    void findByFromAndTo_and_findByFromOrder() {
        repository.deleteAll();
        repository.flush();

        ExchangeRate r1 = new ExchangeRate();
        r1.setFromCurrency("USD");
        r1.setToCurrency("ILS");
        r1.setRate(new BigDecimal("3.67"));
        repository.saveAndFlush(r1);

        ExchangeRate r2 = new ExchangeRate();
        r2.setFromCurrency("USD");
        r2.setToCurrency("EUR");
        r2.setRate(new BigDecimal("0.92"));
        repository.saveAndFlush(r2);

        var found = repository.findByFromCurrencyAndToCurrency("USD", "ILS");
        assertThat(found).isPresent();
        assertThat(found.get().getRate()).isEqualByComparingTo(new BigDecimal("3.67"));

        List<ExchangeRate> list = repository.findByFromCurrencyOrderByToCurrencyAsc("USD");
        assertThat(list).hasSize(2);
        assertThat(list).extracting(ExchangeRate::getToCurrency).containsExactly("EUR", "ILS");
    }
}