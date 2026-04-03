package com.ubik.streakservice.domain.model;

import java.util.List;

public final class AmateurPolicy implements PrivilegePolicy {

    @Override
    public double applyDiscount(double basePrice) { return basePrice * 0.95; }

    @Override
    public double discountRate() { return 0.05; }

    @Override
    public List<String> getBenefits() {
        return List.of(
            "Descuento del 5% en reservas",
            "Acceso anticipado a promociones",
            "Historial de reservas mejorado",
            "Soporte estándar"
        );
    }
}
