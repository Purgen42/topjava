package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.Phaser;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
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

        // Cycles (2 passes)
        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);
        System.out.println();

        // Streams (2 passes)
        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000).stream()
                .map(UserMealWithExcess::toString)
                .collect(Collectors.joining("\n")));
        System.out.println();

        // Cycles optional (1 pass)
        mealsTo = filteredByCyclesOptional(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);
        System.out.println();

        // Streams optional (1 pass)
        System.out.println(filteredByStreamsOptional(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000).stream()
                .map(UserMealWithExcess::toString)
                .collect(Collectors.joining("\n")));
    }

    // Two-pass algorithm
    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> totalCaloriesPerDays = new HashMap<>();
        meals.forEach(meal -> totalCaloriesPerDays.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum));
        List<UserMealWithExcess> resultList = new ArrayList<>();
        meals.forEach(meal -> {
            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                resultList.add(convertUserMealToUserMealWithExcess(meal, totalCaloriesPerDays.get(meal.getDateTime().toLocalDate()) > caloriesPerDay));
            }
        });
        return resultList;
    }

    // Uses two streams and intermediary Map
    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> totalCaloriesPerDays = meals.stream()
                .collect(Collectors.groupingBy(m -> m.getDateTime().toLocalDate(),
                        Collectors.summingInt(UserMeal::getCalories)));

        return meals.stream()
                .filter(m -> TimeUtil.isBetweenHalfOpen(m.getDateTime().toLocalTime(), startTime, endTime))
                .map(m -> convertUserMealToUserMealWithExcess(m, totalCaloriesPerDays.get(m.getDateTime().toLocalDate()) > caloriesPerDay))
                .collect(Collectors.toList());
    }

    // Optional task: single pass on meals, start threads for delayed writing
    public static List<UserMealWithExcess> filteredByCyclesOptional(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> totalCaloriesPerDays = new HashMap<>();
        List<UserMealWithExcess> resultList = Collections.synchronizedList(new ArrayList<>());
        Phaser phaser = new Phaser(1);

        meals.forEach(meal -> {
            totalCaloriesPerDays.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum);
            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                phaser.register();
                new Thread(() -> {
                    phaser.arriveAndAwaitAdvance();
                    resultList.add(convertUserMealToUserMealWithExcess(meal, totalCaloriesPerDays.get(meal.getDateTime().toLocalDate()) > caloriesPerDay));
                    phaser.arriveAndDeregister();
                }).start();
            }
        });
        // Signal the threads that all excesses are calculated
        phaser.arriveAndAwaitAdvance();
        // Let threads do their work before returning value
        phaser.arriveAndAwaitAdvance();
        return resultList;
    }

    // Optional task: single stream, custom Collector
    public static List<UserMealWithExcess> filteredByStreamsOptional(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        return meals.stream()
                .collect(new Collector<UserMeal, List<UserMealWithExcess>, List<UserMealWithExcess>>() {
                    private final Map<LocalDate, Integer> totalCaloriesPerDays = new HashMap<>();
                    private final Map<LocalDate, Boolean> excessPerDays = new HashMap<>();
                    private final Map<LocalDate, List<Integer>> resultIndices = new HashMap<>();

                    @Override
                    public Supplier<List<UserMealWithExcess>> supplier() {
                        return ArrayList::new;
                    }

                    @Override
                    public BiConsumer<List<UserMealWithExcess>, UserMeal> accumulator() {
                        return (l, m) -> {
                            LocalDate mealDate = m.getDateTime().toLocalDate();
                            excessPerDays.putIfAbsent(mealDate, false);
                            int calories = totalCaloriesPerDays.merge(mealDate, m.getCalories(), Integer::sum);
                            if (calories > caloriesPerDay) {
                                // If calories for this day previously did not excess limit, but now do
                                if (!excessPerDays.get(mealDate)) {
                                    // Reset all previously added results for that day
                                    resultIndices.getOrDefault(mealDate, new ArrayList<>())
                                            .forEach(i -> {
                                                UserMealWithExcess meal = l.get(i);
                                                l.set(i, new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), true));
                                            });
                                }
                                excessPerDays.put(mealDate, true);
                            }
                            if (TimeUtil.isBetweenHalfOpen(m.getDateTime().toLocalTime(), startTime, endTime)) {
                                resultIndices.putIfAbsent(mealDate, new ArrayList<>());
                                resultIndices.get(mealDate).add(l.size());
                                UserMealWithExcess mealWithExcess = convertUserMealToUserMealWithExcess(m, calories > caloriesPerDay);
                                l.add(mealWithExcess);
                            }
                        };
                    }

                    @Override
                    public BinaryOperator<List<UserMealWithExcess>> combiner() {
                        return (l, r) -> {
                            l.addAll(r);
                            return l;
                        };
                    }

                    @Override
                    public Function<List<UserMealWithExcess>, List<UserMealWithExcess>> finisher() {
                        return Function.identity();
                    }

                    @Override
                    public Set<Characteristics> characteristics() {
                        return Collections.singleton(Characteristics.IDENTITY_FINISH);
                    }
                });
    }

    private static UserMealWithExcess convertUserMealToUserMealWithExcess(UserMeal meal, boolean excess) {
        return new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess);
    }
}
