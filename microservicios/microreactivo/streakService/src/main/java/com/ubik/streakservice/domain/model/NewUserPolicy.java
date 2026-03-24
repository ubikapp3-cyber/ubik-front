package com.ubik.streakservice.domain.model;

import java.util.List;

public final class NewUserPolicy implements PrivilegePolicy {

    @Override
    public double applyDiscount(double basePrice) { return basePrice; }

    @Override
    public double discountRate() { return 0.0; }

    @Override
    public List<String> getBenefits() {
        return List.of(
            "Acceso básico al sistema",
            "Acceso a promociones generales",
            "Incentivo de primera reserva"
        );
    }
}
