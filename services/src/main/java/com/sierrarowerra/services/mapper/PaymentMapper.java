package com.sierrarowerra.services.mapper;

import com.sierrarowerra.model.Payment;
import com.sierrarowerra.model.dto.PaymentDto;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentDto toDto(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setStatus(payment.getStatus());

        return dto;
    }
}
