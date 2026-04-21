package com.swer313.projectstep1.booking;

import java.time.LocalDate;

public interface AvailabilityChecker {

    boolean isAvailable

            (Long roomTypeId, LocalDate checkIn, LocalDate checkOut);

    long remainingUnits(Long roomTypeId, LocalDate checkIn, LocalDate checkOut);
}