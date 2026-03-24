package com.ubik.streakservice.domain.model;

import java.util.List;

/**
 * Strategy: cada nivel implementa su propio cálculo de descuento y beneficios.
 * OCP: agregar PLATINUM no toca las implementaciones existentes.
 */
public interface PrivilegePolicy {
    double applyDiscount(double basePrice);
    List<String> getBenefits();
    double discountRate();
}
