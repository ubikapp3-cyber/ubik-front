package com.ubik.streakservice.domain.model;

import java.util.List;

public final class GoldPolicy implements PrivilegePolicy {

    @Override
    public double applyDiscount(double basePrice) { return basePrice * 0.90; }

    @Override
    public double discountRate() { return 0.10; }

    @Override
    public List<String> getBenefits() {
        return List.of(
            "Descuento del 10% en todas las reservas",
            "Acceso a ofertas exclusivas",
            "Prioridad en disponibilidad (early booking)",
            "Soporte prioritario",
            "Sistema de recompensas (puntos acumulables)"
        );
    }
}
