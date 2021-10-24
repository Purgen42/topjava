package ru.javawebinar.topjava;

import ru.javawebinar.topjava.model.Meal;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.javawebinar.topjava.model.AbstractBaseEntity.START_SEQ;

public class MealTestData {
    // The DB is populated in reverse order
    public static final int USER_MEAL_7_ID = START_SEQ + 2;
    public static final int USER_MEAL_6_ID = START_SEQ + 3;
    public static final int USER_MEAL_5_ID = START_SEQ + 4;
    public static final int USER_MEAL_4_ID = START_SEQ + 5;
    public static final int USER_MEAL_3_ID = START_SEQ + 6;
    public static final int USER_MEAL_2_ID = START_SEQ + 7;
    public static final int USER_MEAL_1_ID = START_SEQ + 8;
    public static final int NOT_FOUND = 13;

    public static final Meal userMeal1 = new Meal(USER_MEAL_1_ID, LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410);
    public static final Meal userMeal2 = new Meal(USER_MEAL_2_ID, LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500);
    public static final Meal userMeal3 = new Meal(USER_MEAL_3_ID, LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000);
    public static final Meal userMeal4 = new Meal(USER_MEAL_4_ID, LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100);
    public static final Meal userMeal5 = new Meal(USER_MEAL_5_ID, LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500);
    public static final Meal userMeal6 = new Meal(USER_MEAL_6_ID, LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000);
    public static final Meal userMeal7 = new Meal(USER_MEAL_7_ID, LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500);

    public static void assertMatch(Meal actual, Meal expected) {
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    public static void assertMatch(Iterable<Meal> actual, Meal... expected) {
        assertMatch(actual, Arrays.asList(expected));
    }

    public static void assertMatch(Iterable<Meal> actual, Iterable<Meal> expected) {
        assertThat(actual).usingRecursiveFieldByFieldElementComparator().isEqualTo(expected);
    }

    public static Meal getNew() {
        return new Meal(LocalDateTime.of(2021, Month.JANUARY, 1, 10, 0), "New", 100);
    }

    public static Meal getUpdated() {
        Meal updated = new Meal(userMeal1);
        updated.setDescription("Updated descrition");
        updated.setDateTime(LocalDateTime.of(2020, Month.NOVEMBER, 11, 11, 11));
        updated.setCalories(333);
        return updated;
    }

    public static Meal getUpdatedNotFound() {
        Meal updated = getUpdated();
        updated.setId(NOT_FOUND);
        return updated;
    }
}
