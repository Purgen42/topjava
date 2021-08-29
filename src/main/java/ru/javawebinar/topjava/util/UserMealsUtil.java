package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000).stream()
                .map(m -> m.toString() + "\n")
                .collect(Collectors.joining()));

    }

    // Two-pass algorithm
    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        if (meals == null) return null;
        Map<LocalDate, Integer> totalCaloriesPerDay = new HashMap<>();
        // Calculate actual daily calories
        for (UserMeal meal : meals) {
            LocalDate date = meal.getDateTime().toLocalDate();
            totalCaloriesPerDay.merge(date, meal.getCalories(), Integer::sum);
        }
        List<UserMealWithExcess> resultList = new ArrayList<>();
        // Filter source list and convert to DTO
        for (UserMeal meal : meals) {
            LocalTime time = meal.getDateTime().toLocalTime();
            if (TimeUtil.isBetweenHalfOpen(time, startTime, endTime)) {
                LocalDate date = meal.getDateTime().toLocalDate();
                boolean excess = totalCaloriesPerDay.get(date) > caloriesPerDay;
                resultList.add(userMealsToUserMealsWithExcess(meal, excess));
            }
        }
        return resultList;
    }

    // Uses two streams with intermediary Map
    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> totalCaloriesPerDay = meals.stream()
                .collect(Collectors.groupingBy(m -> m.getDateTime().toLocalDate(),
                        Collectors.summingInt(UserMeal::getCalories)));

        return meals.stream()
                .filter(m -> (TimeUtil.isBetweenHalfOpen(m.getDateTime().toLocalTime(), startTime, endTime)))
                .map(m -> userMealsToUserMealsWithExcess(m, totalCaloriesPerDay.get(m.getDateTime().toLocalDate()) > caloriesPerDay))
                .collect(Collectors.toList());
    }

    // Convert UserMeal (entity) to UserMealWithExcess(DTO)
    private static UserMealWithExcess userMealsToUserMealsWithExcess(UserMeal meal, boolean excess) {
        LocalDateTime dateTime = meal.getDateTime();
        String description = meal.getDescription();
        int calories = meal.getCalories();
        return new UserMealWithExcess(dateTime, description, calories, excess);
    }
}
