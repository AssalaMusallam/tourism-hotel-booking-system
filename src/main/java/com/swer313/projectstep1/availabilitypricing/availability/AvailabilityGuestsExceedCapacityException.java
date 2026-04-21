package com.swer313.projectstep1.availabilitypricing.availability;

import com.swer313.projectstep1.errors.BadRequestException;

/**
 * تُرمى لما عدد الضيوف يتجاوز capacity الغرفة.
 * كانت قبل تُرمى كـ AvailabilityDateRangeException — هاد أوضح semantically.
 */
public class AvailabilityGuestsExceedCapacityException extends BadRequestException {

    public AvailabilityGuestsExceedCapacityException(int requested, int capacity) {
        super("Requested guests (" + requested
                + ") exceed room capacity (" + capacity + ").");
    }
}