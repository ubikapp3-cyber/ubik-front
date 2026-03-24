package com.ubik.aiservice.domain.model;

public enum Intent {

    GET_MY_PROFILE(true),
    GET_MY_MOTELS(true),
    GET_MY_RESERVATIONS(true),

    GET_PUBLIC_MOTELS(false),
    GET_ROOMS_BY_MOTEL(false),

    TUTORIAL_LOGIN(false),
    TUTORIAL_REGISTER(false),

    GENERAL_INFO(false),
    UNKNOWN(false);

    private final boolean requiresAuth;

    Intent(boolean requiresAuth) {
        this.requiresAuth = requiresAuth;
    }

    public boolean requiresAuth() {
        return requiresAuth;
    }

    public static Intent fromString(String value) {
        try {
            return Intent.valueOf(value);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }
}
