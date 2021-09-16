package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        // Cycles (2 passes)
        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

        // Streams (2 passes)
        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000).stream()
                .map(UserMealWithExcess::toString)
                .collect(Collectors.joining("\n")));

        // Cycles optional (1 pass)
        mealsTo = filteredByCyclesOptional(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

        // Streams optional (1 pass)
        System.out.println(filteredByStreamsOptional(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000).stream()
                .map(UserMealWithExcess::toString)
                .collect(Collectors.joining("\n")));

    }

    // Two-pass algorithm
    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> totalCaloriesPerDay = new HashMap<>();
        // Calculate actual daily calories
        meals.forEach(meal ->
        {
            LocalDate date = meal.getDateTime().toLocalDate();
            totalCaloriesPerDay.merge(date, meal.getCalories(), Integer::sum);
        });
        List<UserMealWithExcess> resultList = new ArrayList<>();
        // Filter source list and convert to DTO
        meals.forEach(meal ->
        {
            LocalTime time = meal.getDateTime().toLocalTime();
            if (TimeUtil.isBetweenHalfOpen(time, startTime, endTime)) {
                LocalDate date = meal.getDateTime().toLocalDate();
                boolean excess = totalCaloriesPerDay.get(date) > caloriesPerDay;
                resultList.add(userMealsToUserMealsWithExcess(meal, excess));
            }
        });
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

    // Optional task: single pass on meals, additional passes on sublists
    public static List<UserMealWithExcess> filteredByCyclesOptional(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> totalCaloriesPerDay = new HashMap<>();
        Map<LocalDate, Boolean> exceed = new HashMap<>();
        Map<LocalDate, List<UserMeal>> dailyMeals = new HashMap<>();
        // Map to store resultList indices
        Map<LocalDateTime, Integer> resultIndex = new HashMap<>();
        List<UserMealWithExcess> resultList = new ArrayList<>();

        meals.forEach(meal ->
        {
            LocalDate date = meal.getDateTime().toLocalDate();
            exceed.putIfAbsent(date, false);
            List<UserMeal> mealsThisDay = dailyMeals.computeIfAbsent(date, k -> new ArrayList<>());
            int newCalories = totalCaloriesPerDay.merge(date, meal.getCalories(), Integer::sum);
            if (newCalories > caloriesPerDay) {
                if (!exceed.get(date)) {
                    // if this meal overweights calories limit for this day, we reset all previous meals for this day with excess = true ones
                    exceed.put(date, true);
                    // runs once per each item at most, so no increase of complexity (still O(N))
                    mealsThisDay.forEach(m -> {
                        if (TimeUtil.isBetweenHalfOpen(m.getDateTime().toLocalTime(), startTime, endTime)) {
                            // Getting resultList indices that are already set for the day and resetting them in resultList
                            Integer index = resultIndex.get(m.getDateTime());
                            if (index != null) resultList.set(index, userMealsToUserMealsWithExcess(m, true));
                        }
                    });
                }
            }
            mealsThisDay.add(meal);
            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                resultList.add(userMealsToUserMealsWithExcess(meal, exceed.get(date)));
                resultIndex.put(meal.getDateTime(), resultList.size() - 1);
            }
        });
        return resultList;
    }

    // Optional task: single stream, custom Collector
    public static List<UserMealWithExcess> filteredByStreamsOptional(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
         return meals.stream()
                 .collect(summingCaloriesCollector(startTime, endTime, caloriesPerDay));
    }

    private static Collector<UserMeal, List<UserMeal>, List<UserMealWithExcess>> summingCaloriesCollector(LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        return new Collector<UserMeal, List<UserMeal>, List<UserMealWithExcess>>() {
            private final Map<LocalDate, Integer> totalCaloriesPerDay = new HashMap<>();
            private Map<LocalDate, Boolean> excessPerDay = new HashMap<>();
            @Override
            public Supplier<List<UserMeal>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<List<UserMeal>, UserMeal> accumulator() {
                        return (l, m) -> {
                            LocalDate mealDate = m.getDateTime().toLocalDate();
                            Integer calories = totalCaloriesPerDay.merge(mealDate, m.getCalories(), Integer::sum);
                            if (calories > caloriesPerDay) {
                                excessPerDay.put(mealDate, true);
                            }
                        };
            }

            @Override
            public BinaryOperator<List<UserMeal>> combiner() {
                return (l, r) -> {
                    l.addAll(r);
                    return l;
                };
            }

            @Override
            public Function<List<UserMeal>, List<UserMealWithExcess>> finisher() {
                return l -> l.stream()
                        .filter(m -> TimeUtil.isBetweenHalfOpen(m.getDateTime().toLocalTime(), startTime, endTime))
                        .map(m -> userMealsToUserMealsWithExcess(m, excessPerDay.getOrDefault(m.getDateTime().toLocalDate(), false)))
                        .collect(Collectors.toList());
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
    }


    // Convert UserMeal (entity) to UserMealWithExcess(DTO)
    private static UserMealWithExcess userMealsToUserMealsWithExcess(UserMeal meal, boolean excess) {
        LocalDateTime dateTime = meal.getDateTime();
        String description = meal.getDescription();
        int calories = meal.getCalories();
        return new UserMealWithExcess(dateTime, description, calories, excess);
    }
}
