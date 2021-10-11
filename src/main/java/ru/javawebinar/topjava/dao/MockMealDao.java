package ru.javawebinar.topjava.dao;

import ru.javawebinar.topjava.model.Meal;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MockMealDao implements MealDao {
    private final ConcurrentMap<Integer, Meal> mockDataSource;
    private final AtomicInteger counter;

    private final static List<Meal> testMealsList = Arrays.asList(
            new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
            new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
            new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
            new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
            new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
            new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
            new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
    );

    public MockMealDao() {
        mockDataSource = new ConcurrentHashMap<>();
        counter = new AtomicInteger(1);
        testMealsList.forEach(this::add);
    }

    @Override
    public Meal add(Meal meal) {
        if (meal.getId() != null) return null;
        int id = counter.getAndIncrement();
        Meal newMeal = new Meal(id, meal.getDateTime(), meal.getDescription(), meal.getCalories());
        mockDataSource.put(id, newMeal);
        return newMeal;
    }

    @Override
    public List<Meal> getAll() {
        return new ArrayList<>(mockDataSource.values());
    }

    @Override
    public Meal getById(int id) {
        return mockDataSource.get(id);
    }

    @Override
    public Meal update(Meal meal) {
        return mockDataSource.replace(meal.getId(), meal) == null ? null : meal;
    }

    @Override
    public void delete(int id) {
        mockDataSource.remove(id);
    }
}
