package com.swer313.projectstep1.availabilitypricing.currency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    /**
     * جلب سعر الصرف بين عملتين.
     * مثال: findByFromCurrencyAndToCurrency("USD", "ILS")
     * بترجع Optional عشان نعرف لو العملة مش موجودة.
     */
    Optional<ExchangeRate> findByFromCurrencyAndToCurrency(
            String fromCurrency,
            String toCurrency
    );

    /**
     * كل أسعار الصرف من عملة معينة.
     * مثال: findByFromCurrency("USD") → كل العملات اللي نقدر نحول لها من USD.
     */
    List<ExchangeRate> findByFromCurrencyOrderByToCurrencyAsc(String fromCurrency);
}