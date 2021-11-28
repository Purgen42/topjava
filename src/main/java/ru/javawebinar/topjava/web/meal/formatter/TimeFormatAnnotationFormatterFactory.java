package ru.javawebinar.topjava.web.meal.formatter;

import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import java.time.LocalTime;
import java.util.Set;

public class TimeFormatAnnotationFormatterFactory implements AnnotationFormatterFactory<TimeFormat> {
    @Override
    public Set<Class<?>> getFieldTypes() {
        return Set.of(LocalTime.class);
    }

    @Override
    public Printer<?> getPrinter(TimeFormat annotation, Class<?> fieldType) {
        return new LocalTimeFormatter();
    }

    @Override
    public Parser<?> getParser(TimeFormat annotation, Class<?> fieldType) {
        return new LocalTimeFormatter();
    }
}
