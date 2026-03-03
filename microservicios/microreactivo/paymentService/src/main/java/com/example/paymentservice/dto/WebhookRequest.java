package com.example.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WebhookRequest(
        String action,          // "payment.created", "payment.updated"
        String type,            // "payment"
        @JsonProperty("data")
        WebhookData data
) {
    public record WebhookData(Long id) {}
}