package com.swer313.projectstep1.availabilitypricing.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeder لأسعار الصرف.
 * بيشتغل تلقائياً لما المشروع يبدأ.
 * بيتحقق إن الجدول فارغ قبل ما يضيف — ما يكرر البيانات.
 * نفس نمط LoadRoomDatabase الموجود في المشروع.
 */
@Configuration
public class LoadExchangeRateDatabase {

    private static final Logger log =
            LoggerFactory.getLogger(LoadExchangeRateDatabase.class);

    @Bean
    CommandLineRunner initExchangeRates(ExchangeRateRepository exchangeRateRepository) {
        return args -> {

            // لو في بيانات موجودة → تخطى — ما نكرر
            if (exchangeRateRepository.count() > 0) {
                log.info("Exchange rates already seeded — skipping.");
                return;
            }

            List<ExchangeRate> rates = List.of(
                    rate("USD", "ILS", "3.670000"),  // شيكل إسرائيلي
                    rate("USD", "JOD", "0.710000"),  // دينار أردني
                    rate("USD", "EUR", "0.920000"),  // يورو
                    rate("USD", "GBP", "0.790000"),  // جنيه إسترليني
                    rate("USD", "SAR", "3.750000")   // ريال سعودي
            );

            exchangeRateRepository.saveAll(rates);

            log.info("Seeded {} exchange rates successfully.", rates.size());
        };
    }

    // Helper method — يبني الـ ExchangeRate object
    private ExchangeRate rate(String from, String to, String rateValue) {
        ExchangeRate er = new ExchangeRate();
        er.setFromCurrency(from);
        er.setToCurrency(to);
        er.setRate(new BigDecimal(rateValue));
        er.setUpdatedAt(LocalDateTime.now());
        return er;
    }
}