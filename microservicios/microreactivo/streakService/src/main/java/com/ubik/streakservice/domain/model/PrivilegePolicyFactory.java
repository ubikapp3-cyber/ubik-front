package com.ubik.streakservice.domain.model;

/**
 * Factory stateless. Agregar PLATINUM solo requiere añadir un case aquí
 * y una nueva implementación de PrivilegePolicy. Nada más cambia.
 */
public final class PrivilegePolicyFactory {

    private PrivilegePolicyFactory() {}

    public static PrivilegePolicy getPolicy(StreakLevel level) {
        return switch (level) {
            case GOLD    -> new GoldPolicy();
            case AMATEUR -> new AmateurPolicy();
            default      -> new NewUserPolicy();
        };
    }
}
