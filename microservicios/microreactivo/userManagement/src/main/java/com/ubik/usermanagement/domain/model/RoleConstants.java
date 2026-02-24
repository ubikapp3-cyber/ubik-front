package com.ubik.usermanagement.domain.model;


public final class RoleConstants {
    public static final long ADMIN = Long.parseLong(
            System.getenv().getOrDefault("ROLE_ID_ADMIN", "7392841056473829"));
    public static final long PROPERTY_OWNER = Long.parseLong(
            System.getenv().getOrDefault("ROLE_ID_PROPERTY_OWNER", "3847261094857362"));
    public static final long USER = Long.parseLong(
            System.getenv().getOrDefault("ROLE_ID_USER", "9182736450192837"));
}