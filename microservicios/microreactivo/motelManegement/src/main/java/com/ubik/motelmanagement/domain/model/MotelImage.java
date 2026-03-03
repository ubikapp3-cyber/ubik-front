package com.ubik.motelmanagement.domain.model;


public record MotelImage(
        Integer id,
        String url,
        ImageRole role,
        Integer sortOrder
) {}
