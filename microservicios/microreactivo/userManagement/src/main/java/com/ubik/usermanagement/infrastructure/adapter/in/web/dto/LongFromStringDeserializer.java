package com.ubik.usermanagement.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public class LongFromStringDeserializer extends StdDeserializer<Long> {
    public LongFromStringDeserializer() { super(Long.class); }

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        return Long.parseLong(p.getText().trim());
    }
}