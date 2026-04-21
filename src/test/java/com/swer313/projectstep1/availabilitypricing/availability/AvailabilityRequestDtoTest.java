package com.swer313.projectstep1.availabilitypricing.availability;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AvailabilityRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void gettersAndSetters_andIsCheckOutAfterCheckIn_behaviour() {
        // Arrange: create DTO and set fields
        AvailabilityRequestDto dto = new AvailabilityRequestDto();
        dto.setRoomTypeId(5L);
        dto.setCheckIn(LocalDate.of(2026, 4, 10));
        dto.setCheckOut(LocalDate.of(2026, 4, 12));
        dto.setGuests(2);

        // Act & Assert: getters return same values
        assertEquals(5L, dto.getRoomTypeId());
        assertEquals(LocalDate.of(2026,4,10), dto.getCheckIn());
        assertEquals(LocalDate.of(2026,4,12), dto.getCheckOut());
        assertEquals(2, dto.getGuests());

        // isCheckOutAfterCheckIn should be true for valid dates
        assertTrue(dto.isCheckOutAfterCheckIn());
    }

    @Test
    void isCheckOutAfterCheckIn_returnsTrue_whenEitherDateNull() {
        AvailabilityRequestDto dto = new AvailabilityRequestDto();
        dto.setCheckIn(null);
        dto.setCheckOut(LocalDate.of(2026,4,12));
        assertTrue(dto.isCheckOutAfterCheckIn());

        dto.setCheckIn(LocalDate.of(2026,4,10));
        dto.setCheckOut(null);
        assertTrue(dto.isCheckOutAfterCheckIn());
    }

    @Test
    void isCheckOutAfterCheckIn_returnsFalse_whenCheckOutNotAfterCheckIn() {
        AvailabilityRequestDto dto = new AvailabilityRequestDto();
        dto.setCheckIn(LocalDate.of(2026,4,10));
        dto.setCheckOut(LocalDate.of(2026,4,10));
        assertFalse(dto.isCheckOutAfterCheckIn());
    }

    @Test
    void beanValidation_detectsMissingFieldsAndInvalidGuests() {
        AvailabilityRequestDto dto = new AvailabilityRequestDto();
        dto.setRoomTypeId(null);
        dto.setCheckIn(null);
        dto.setCheckOut(null);
        dto.setGuests(0);

        Set<ConstraintViolation<AvailabilityRequestDto>> violations = validator.validate(dto);
        // Expect violations for roomTypeId, checkIn, checkOut, and guests (min=1)
        assertTrue(violations.size() >= 4);
    }
}

