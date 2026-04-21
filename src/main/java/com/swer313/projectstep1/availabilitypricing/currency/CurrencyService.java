package com.swer313.projectstep1.availabilitypricing.currency;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CurrencyService {

    private final ExchangeRateRepository exchangeRateRepository;

    public CurrencyService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    /**
     * يحوّل مبلغ من عملة لعملة.
     *
     * الـ logic:
     * 1. لو نفس العملة → ارجع نفس المبلغ بدون حساب
     * 2. جيب الـ rate من الداتابيس
     * 3. اضرب المبلغ بالـ rate
     * 4. قرّب لـ decimal واحدين: 125.333 → 125.33
     *
     * مثال: convert(100, "USD", "ILS")
     * → 100 × 3.67 = 367.00 ILS
     */
    public BigDecimal convert(BigDecimal amount,
                              String from,
                              String to) {
        // نفس العملة — ما في داعي للحساب
        if (from.equalsIgnoreCase(to)) {
            return amount;
        }

        // جيب الـ rate من الداتابيس
        ExchangeRate rate = exchangeRateRepository
                .findByFromCurrencyAndToCurrency(
                        from.toUpperCase(),
                        to.toUpperCase()
                )
                .orElseThrow(() -> new CurrencyNotFoundException(from, to));

        // السعر المحوّل = المبلغ × سعر الصرف
        return amount
                .multiply(rate.getRate())
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * كل العملات المدعومة من USD.
     * يستخدمه الـ endpoint اللي يعرض للـ guest شو العملات المتاحة.
     */
    public List<ExchangeRate> getSupportedCurrencies() {
        return exchangeRateRepository
                .findByFromCurrencyOrderByToCurrencyAsc("USD");
    }
}