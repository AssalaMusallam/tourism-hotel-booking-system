package com.swer313.projectstep1.availabilitypricing.currency;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * الـ response لما الـ guest يطلب السعر بعملة معينة.
 *
 * فيه:
 * - السعر الأصلي بالـ USD (عشان يكون شفاف)
 * - السعر المحوّل بالعملة المطلوبة
 * - سعر الصرف المستخدم
 * - عدد الليالي
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceInCurrencyResponseDto {

    private final Long       roomTypeId;
    private final String     roomTypeName;
    private final long       nights;

    // السعر الأصلي بالـ USD
    private final BigDecimal originalTotalUSD;

    // السعر المحوّل
    private final BigDecimal convertedTotal;
    private final String     currency;

    // سعر الصرف المستخدم — للشفافية
    private final BigDecimal exchangeRate;

    public PriceInCurrencyResponseDto(Long roomTypeId, String roomTypeName,
                                      long nights,
                                      BigDecimal originalTotalUSD,
                                      BigDecimal convertedTotal,
                                      String currency,
                                      BigDecimal exchangeRate) {
        this.roomTypeId       = roomTypeId;
        this.roomTypeName     = roomTypeName;
        this.nights           = nights;
        this.originalTotalUSD = originalTotalUSD;
        this.convertedTotal   = convertedTotal;
        this.currency         = currency;
        this.exchangeRate     = exchangeRate;
    }

    public Long       getRoomTypeId()        { return roomTypeId; }
    public String     getRoomTypeName()      { return roomTypeName; }
    public long       getNights()            { return nights; }
    public BigDecimal getOriginalTotalUSD()  { return originalTotalUSD; }
    public BigDecimal getConvertedTotal()    { return convertedTotal; }
    public String     getCurrency()          { return currency; }
    public BigDecimal getExchangeRate()      { return exchangeRate; }
}