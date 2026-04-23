package com.auctionapp.auctionappjava.common.util;

import com.auctionapp.auctionappjava.common.dto.ApiEnvelope;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

/**
 * Dependency-free JSON helper for the socket protocol.  It keeps the transport
 * line-delimited JSON while serializing the ApiEnvelope payload safely without
 * adding external libraries to the existing Maven project.
 */
public final class JacksonSupport {
    private JacksonSupport() {
    }

    public static String toJson(Object value) {
        String encoded = Base64.getEncoder().encodeToString(serialize(value));
        StringBuilder json = new StringBuilder(256 + encoded.length());
        json.append('{');
        if (value instanceof ApiEnvelope<?> envelope) {
            append(json, "kind", envelope.getKind() == null ? null : envelope.getKind().name()).append(',');
            append(json, "action", envelope.getAction() == null ? null : envelope.getAction().name()).append(',');
            append(json, "correlationId", envelope.getCorrelationId() == null ? null : envelope.getCorrelationId().toString()).append(',');
            json.append("\"success\":").append(envelope.isSuccess()).append(',');
        }
        append(json, "payload", encoded);
        json.append('}');
        return json.toString();
    }

    public static <T> T fromJson(String json, Class<T> targetType) {
        Object value = deserialize(Base64.getDecoder().decode(extract(json, "payload")));
        return convertValue(value, targetType);
    }

    public static ApiEnvelope<Serializable> envelopeFromJson(String json) {
        @SuppressWarnings("unchecked")
        ApiEnvelope<Serializable> envelope = (ApiEnvelope<Serializable>) fromJson(json, ApiEnvelope.class);
        return envelope;
    }

    public static <T> T convertValue(Object value, Class<T> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return targetType.cast(value);
        }
        throw new IllegalArgumentException("Không thể chuyển " + value.getClass().getName() + " sang " + targetType.getName());
    }

    public static <T> List<T> convertList(Object value, Class<T> elementType) {
        if (!(value instanceof Collection<?> collection)) {
            return List.of();
        }
        List<T> result = new ArrayList<>(collection.size());
        for (Object item : collection) {
            result.add(convertValue(item, elementType));
        }
        return result;
    }

    private static StringBuilder append(StringBuilder json, String key, String value) {
        json.append('"').append(escape(key)).append("\":");
        if (value == null) {
            json.append("null");
        } else {
            json.append('"').append(escape(value)).append('"');
        }
        return json;
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String extract(String json, String key) {
        String token = "\"" + key + "\"";
        int keyIndex = json.indexOf(token);
        if (keyIndex < 0) {
            throw new IllegalArgumentException("JSON thiếu key " + key);
        }
        int colon = json.indexOf(':', keyIndex + token.length());
        int firstQuote = json.indexOf('"', colon + 1);
        int i = firstQuote + 1;
        StringBuilder out = new StringBuilder();
        while (i < json.length()) {
            char c = json.charAt(i++);
            if (c == '\\') {
                if (i >= json.length()) {
                    break;
                }
                char e = json.charAt(i++);
                out.append(switch (e) {
                    case 'n' -> '\n';
                    case 'r' -> '\r';
                    case 't' -> '\t';
                    case '"' -> '"';
                    case '\\' -> '\\';
                    default -> e;
                });
            } else if (c == '"') {
                return out.toString();
            } else {
                out.append(c);
            }
        }
        throw new IllegalArgumentException("JSON payload không hợp lệ: " + json);
    }

    private static byte[] serialize(Object value) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
            out.flush();
            return bytes.toByteArray();
        } catch (Exception e) {
            throw new IllegalArgumentException("Không thể serialize JSON payload", e);
        }
    }

    private static Object deserialize(byte[] bytes) {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return in.readObject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Không thể deserialize JSON payload", e);
        }
    }
}
