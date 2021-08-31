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
                .map(UserMealWithExcess::toString)
                .collect(Collectors.joining("\n")));

        mealsTo = filteredByCyclesOptional(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);
    }

    // Two-pass algorithm
    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
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

    // Uses two streams and intermediary Map
    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> totalCaloriesPerDay = meals.stream()
                .collect(Collectors.groupingBy(m -> m.getDateTime().toLocalDate(),
                        Collectors.summingInt(UserMeal::getCalories)));

        return meals.stream()
                .filter(m -> (TimeUtil.isBetweenHalfOpen(m.getDateTime().toLocalTime(), startTime, endTime)))
                .map(m -> userMealsToUserMealsWithExcess(m, totalCaloriesPerDay.get(m.getDateTime().toLocalDate()) > caloriesPerDay))
                .collect(Collectors.toList());
    }

    // Optional task: single pass on UserMeals list, additional passes on sublists
    public static List<UserMealWithExcess> filteredByCyclesOptional(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> totalCaloriesPerDay = new HashMap<>();
        Map<LocalDate, Boolean> exceed = new HashMap<>();
        Map<LocalDate, List<UserMeal>> dailyMeals = new HashMap<>();
        Map<LocalDateTime, UserMealWithExcess> resultMap = new HashMap<>();

        for (UserMeal meal : meals) {
            LocalDate date = meal.getDateTime().toLocalDate();
            exceed.putIfAbsent(date, false);
            List<UserMeal> mealsThisDay = dailyMeals.computeIfAbsent(date, k -> new ArrayList<>());
            int newCalories = totalCaloriesPerDay.merge(date, meal.getCalories(), Integer::sum);
            if (newCalories > caloriesPerDay) {
                if (!exceed.get(date)) {
                    // if this meal overweights calories limit for this day, we reset all previous meals for this day with excess = true ones
                    exceed.put(date, true);
                    // runs once per each sublist at most, so no increase of complexity
                    mealsThisDay.forEach(m -> {if (TimeUtil.isBetweenHalfOpen(m.getDateTime().toLocalTime(), startTime, endTime))
                        resultMap.put(m.getDateTime(), userMealsToUserMealsWithExcess(m, true));});
                }
            }
            mealsThisDay.add(meal);
            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime))
                resultMap.put(meal.getDateTime(), userMealsToUserMealsWithExcess(meal, exceed.get(date)));
        }
        return new ArrayList<>(resultMap.values());
    }


    // Convert UserMeal (entity) to UserMealWithExcess(DTO)
    private static UserMealWithExcess userMealsToUserMealsWithExcess(UserMeal meal, boolean excess) {
        LocalDateTime dateTime = meal.getDateTime();
        String description = meal.getDescription();
        int calories = meal.getCalories();
        return new UserMealWithExcess(dateTime, description, calories, excess);
    }
}
