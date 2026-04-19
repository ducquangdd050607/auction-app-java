package com.auctionapp.auctionappjava.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JacksonSupport {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private JacksonSupport() {
    }

    public static ObjectMapper mapper() {
        return OBJECT_MAPPER;
    }

    public static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot serialize JSON", ex);
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot deserialize JSON", ex);
        }
    }

    public static JsonNode toNode(Object value) {
        return OBJECT_MAPPER.valueToTree(value);
    }

    public static <T> T convert(JsonNode node, Class<T> type) {
        return OBJECT_MAPPER.convertValue(node, type);
    }
}
