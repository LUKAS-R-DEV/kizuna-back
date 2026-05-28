package com.kizuna.data_service.metrics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class MetricsPeriodResolver {

    private static final LocalDateTime EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private MetricsPeriodResolver() {
    }

    public record Period(LocalDateTime from, LocalDateTime to, String label) {
    }

    public static Period resolve(String period, String fromParam, String toParam) {
        LocalDateTime to = parseDateTime(toParam, LocalDateTime.now());
        LocalDateTime from;

        String preset = period == null || period.isBlank() ? "30d" : period.trim().toLowerCase();
        switch (preset) {
            case "today" -> {
                from = LocalDate.now().atStartOfDay();
                preset = "today";
            }
            case "7d" -> from = to.minusDays(7);
            case "30d" -> from = to.minusDays(30);
            case "all" -> {
                from = EPOCH;
                preset = "all";
            }
            default -> {
                if (fromParam != null && !fromParam.isBlank()) {
                    from = parseDateTime(fromParam, to.minusDays(30));
                    preset = "custom";
                } else {
                    from = to.minusDays(30);
                    preset = "30d";
                }
            }
        }

        if (from.isAfter(to)) {
            LocalDateTime swap = from;
            from = to;
            to = swap;
        }

        return new Period(from, to, preset);
    }

    private static LocalDateTime parseDateTime(String value, LocalDateTime fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            if (value.length() == 10) {
                return LocalDate.parse(value).atStartOfDay();
            }
            return LocalDateTime.parse(value, ISO);
        } catch (DateTimeParseException ex) {
            return fallback;
        }
    }
}
