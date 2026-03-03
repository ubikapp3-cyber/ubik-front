package com.ubik.paymentservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WebhookRequest(
        String type,
        String action,
        @JsonProperty("data") WebhookData data
) {
    public String dataId() {
        return data != null ? data.id() : null;
    }

    public record WebhookData(String id) {}
}
