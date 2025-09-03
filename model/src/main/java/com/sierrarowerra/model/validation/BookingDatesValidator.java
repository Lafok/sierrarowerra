package com.sierrarowerra.model.validation;

import com.sierrarowerra.model.dto.booking.BookingRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BookingDatesValidator implements ConstraintValidator<ValidBookingDates, BookingRequestDto> {

    @Override
    public boolean isValid(BookingRequestDto dto, ConstraintValidatorContext context) {
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            // We don't handle null checks here, that's for @NotNull
            return true;
        }
        return dto.getEndDate().isAfter(dto.getStartDate());
    }
}
