package com.swer313.projectstep1.payment;

import java.math.BigDecimal;
import java.util.Map;

/** يُرجعه GET /api/payments/stats */
public class PaymentStatsDTO {

    private final long       total;
    private final long       pending;
    private final long       successful;
    private final long       failed;
    private final long       refunded;
    private final BigDecimal totalRevenue;   // مجموع SUCCESS فقط

    public PaymentStatsDTO(
            long total, long pending, long successful,
            long failed, long refunded, BigDecimal totalRevenue
    ) {
        this.total        = total;
        this.pending      = pending;
        this.successful   = successful;
        this.failed       = failed;
        this.refunded     = refunded;
        this.totalRevenue = totalRevenue;
    }

    public long       getTotal()        { return total; }
    public long       getPending()      { return pending; }
    public long       getSuccessful()   { return successful; }
    public long       getFailed()       { return failed; }
    public long       getRefunded()     { return refunded; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
}