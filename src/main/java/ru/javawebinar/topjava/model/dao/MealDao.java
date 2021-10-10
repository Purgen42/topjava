package ru.javawebinar.topjava.model.dao;

import ru.javawebinar.topjava.model.Meal;

import java.util.List;

public interface MealDao {
    void add(Meal meal);

    List<Meal> getAll();

    Meal getId(int id);

    void update(int id, Meal meal);

    void delete(int id);
}
