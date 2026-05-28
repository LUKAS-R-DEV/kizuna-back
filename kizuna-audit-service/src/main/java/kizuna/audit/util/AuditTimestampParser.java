package kizuna.audit.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public final class AuditTimestampParser {

    private AuditTimestampParser() {
    }

    public static LocalDateTime parse(Object raw) {
        if (raw == null) {
            return LocalDateTime.now(ZoneOffset.UTC);
        }
        if (raw instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (raw instanceof String text) {
            String trimmed = text.trim();
            if (trimmed.isEmpty()) {
                return LocalDateTime.now(ZoneOffset.UTC);
            }
            if (trimmed.endsWith("Z") || trimmed.contains("+")) {
                return LocalDateTime.ofInstant(Instant.parse(trimmed), ZoneOffset.UTC);
            }
            return LocalDateTime.parse(trimmed);
        }
        if (raw instanceof List<?> list && list.size() >= 3) {
            int year = toInt(list.get(0));
            int month = toInt(list.get(1));
            int day = toInt(list.get(2));
            int hour = list.size() > 3 ? toInt(list.get(3)) : 0;
            int minute = list.size() > 4 ? toInt(list.get(4)) : 0;
            int second = list.size() > 5 ? toInt(list.get(5)) : 0;
            return LocalDateTime.of(year, month, day, hour, minute, second);
        }
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    private static int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
