package ru.javawebinar.topjava.web.meal.formatter;

import org.springframework.format.Formatter;

import java.text.ParseException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class LocalTimeFormatter implements Formatter<LocalTime> {
    @Override
    public LocalTime parse(String text, Locale locale) throws ParseException {
        try {
            return LocalTime.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    public String print(LocalTime object, Locale locale) {
        return object.toString();
    }
}
