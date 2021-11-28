package ru.javawebinar.topjava.formatter;

import org.springframework.format.Formatter;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.time.LocalTime;
import java.util.Locale;

public class LocalTimeFormatter implements Formatter<LocalTime> {
    @Override
    public LocalTime parse(String text, Locale locale) throws ParseException {
        return StringUtils.hasLength(text) ? LocalTime.parse(text) : null;
    }

    @Override
    public String print(LocalTime object, Locale locale) {
        return object.toString();
    }
}
