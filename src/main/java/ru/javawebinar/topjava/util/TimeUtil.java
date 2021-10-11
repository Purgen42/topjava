package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.Meal;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

public class TimeUtil {
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static Predicate<Meal> isBetweenHalfOpen(LocalTime startTime, LocalTime endTime) {
        return m -> m.getTime().compareTo(startTime) >= 0 && m.getTime().compareTo(endTime) < 0;
    }

    public static Predicate<Meal> allEntries() {
        return m -> true;
    }

    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(dateTimeFormatter);
    }
}
