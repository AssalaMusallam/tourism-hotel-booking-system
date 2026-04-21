package com.swer313.projectstep1.booking;

import com.swer313.projectstep1.errors.NotFoundException;

// ── 404 ──────────────────────────────────────────────────────────────────────

public class BookingNotFoundException extends NotFoundException {
    public BookingNotFoundException(Long id) {
        super("Booking with id " + id + " was not found.");
    }
}
