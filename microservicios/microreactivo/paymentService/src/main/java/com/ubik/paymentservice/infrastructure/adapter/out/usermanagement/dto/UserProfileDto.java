package com.ubik.paymentservice.infrastructure.adapter.out.usermanagement.dto;

public record UserProfileDto(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String documentNumber,
        String phone
) {}
