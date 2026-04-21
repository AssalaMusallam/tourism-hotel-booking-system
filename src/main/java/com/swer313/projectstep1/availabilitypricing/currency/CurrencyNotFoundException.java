package com.swer313.projectstep1.availabilitypricing.currency;

import com.swer313.projectstep1.errors.BadRequestException;

/**
 * يُرمى لما الـ guest يطلب عملة مش موجودة في الداتابيس.
 * مثال: currency=JPY وما عندنا سعر لليين الياباني.
 */
public class CurrencyNotFoundException extends BadRequestException {
    public CurrencyNotFoundException(String from, String to) {
        super("Exchange rate not found: " + from + " → " + to +
                ". Supported currencies: USD, ILS, JOD, EUR, GBP, SAR");
    }
}