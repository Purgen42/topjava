package ru.javawebinar.topjava.formatter;

import org.springframework.format.Formatter;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Locale;

public class LocalDateFormatter implements Formatter<LocalDate> {
    @Override
    public LocalDate parse(String text, Locale locale) throws ParseException {
        return StringUtils.hasLength(text) ? LocalDate.parse(text) : null;
    }

    @Override
    public String print(LocalDate object, Locale locale) {
        return object.toString();
    }
}
