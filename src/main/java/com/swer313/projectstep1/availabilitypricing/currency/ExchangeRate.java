package com.swer313.projectstep1.availabilitypricing.currency;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity تخزن أسعار الصرف في الداتابيس.
 * كل row = تحويل من عملة لعملة.
 * مثال: USD → ILS بسعر 3.67
 *
 * الـ unique constraint يمنع تكرار نفس الزوج (USD→ILS مرتين).
 */
@Entity
@Table(
        name = "exchange_rates",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_exchange_rates_from_to",
                columnNames = {"from_currency", "to_currency"}
        )
)
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // العملة المصدر — دايماً USD في مشروعنا
    @Column(name = "from_currency", length = 3, nullable = false)
    private String fromCurrency;

    // العملة الهدف — ILS, JOD, EUR, ...
    @Column(name = "to_currency", length = 3, nullable = false)
    private String toCurrency;

    // سعر الصرف — مثال: 1 USD = 3.67 ILS
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal rate;

    // آخر تحديث — مفيد لو بدنا نضيف auto-refresh مستقبلاً
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ExchangeRate() {}

    public Long          getId()                       { return id; }
    public String        getFromCurrency()             { return fromCurrency; }
    public void          setFromCurrency(String from)  { this.fromCurrency = from; }
    public String        getToCurrency()               { return toCurrency; }
    public void          setToCurrency(String to)      { this.toCurrency = to; }
    public BigDecimal    getRate()                     { return rate; }
    public void          setRate(BigDecimal rate)      { this.rate = rate; }
    public LocalDateTime getUpdatedAt()                { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime t) { this.updatedAt = t; }
}