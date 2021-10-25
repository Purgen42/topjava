package ru.javawebinar.topjava.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import ru.javawebinar.topjava.MealTestData;
import ru.javawebinar.topjava.UserTestData;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.exception.NotFoundException;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static ru.javawebinar.topjava.MealTestData.*;

@ContextConfiguration({
        "classpath:spring/spring-app.xml",
        "classpath:spring/spring-app-jdbc.xml",
        "classpath:spring/spring-db.xml"
})
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:db/populateDB.sql", config = @SqlConfig(encoding = "UTF-8"))
public class MealServiceTest {

    static {
        // Only for postgres driver logging
        // It uses java.util.logging and logged via jul-to-slf4j bridge
        SLF4JBridgeHandler.install();
    }

    @Autowired
    private MealService service;

    @Test
    public void get() {
        Meal meal = service.get(MealTestData.USER_MEAL_1_ID, UserTestData.USER_ID);
        assertMatch(meal, userMeal1);
    }

    @Test
    public void getNotFound() {
        assertThrows(NotFoundException.class, () -> service.get(MealTestData.NOT_FOUND, UserTestData.USER_ID));
    }

    @Test
    public void getWrongUser() {
        assertThrows(NotFoundException.class, () -> service.get(MealTestData.USER_MEAL_1_ID, UserTestData.ADMIN_ID));
    }

    @Test
    public void delete() {
        service.delete(USER_MEAL_1_ID, UserTestData.USER_ID);
        assertThrows(NotFoundException.class, () -> service.get(USER_MEAL_1_ID, UserTestData.USER_ID));
    }

    @Test
    public void deleteNotFound() {
        assertThrows(NotFoundException.class, () -> service.delete(MealTestData.NOT_FOUND, UserTestData.USER_ID));
    }

    @Test
    public void deleteWrongUser() {
        assertThrows(NotFoundException.class, () -> service.delete(USER_MEAL_1_ID, UserTestData.ADMIN_ID));
    }

    @Test
    public void getBetweenInclusive() {
        List<Meal> filtered = service.getBetweenInclusive(userMeal1.getDate(), userMeal1.getDate(), UserTestData.USER_ID);
        assertMatch(filtered, userMeal1, userMeal2, userMeal3, userMeal4);
    }

    @Test
    public void getBetweenInclusiveNullBoundaries() {
        List<Meal> filtered = service.getBetweenInclusive(null, null, UserTestData.USER_ID);
        assertMatch(filtered, userMeal1, userMeal2, userMeal3, userMeal4, userMeal5, userMeal6, userMeal7);
    }

    @Test
    public void getAll() {
        List<Meal> all = service.getAll(UserTestData.USER_ID);
        assertMatch(all, userMeal1, userMeal2, userMeal3, userMeal4, userMeal5, userMeal6, userMeal7);
    }

    @Test
    public void update() {
        Meal updated = getUpdated();
        service.update(updated, UserTestData.USER_ID);
        assertMatch(service.get(USER_MEAL_1_ID, UserTestData.USER_ID), getUpdated());
    }

    @Test
    public void updateNotFound() {
        assertThrows(NotFoundException.class, () -> service.update(getUpdatedNotFound(), UserTestData.USER_ID));
    }

    @Test
    public void updateWrongUser() {
        assertThrows(NotFoundException.class, () -> service.update(getUpdated(), UserTestData.ADMIN_ID));
    }

    @Test
    public void create() {
        Meal created = service.create(getNew(), UserTestData.USER_ID);
        Integer newId = created.getId();
        Meal newMeal = getNew();
        newMeal.setId(newId);
        assertMatch(created, newMeal);
        assertMatch(service.get(newId, UserTestData.USER_ID), newMeal);
    }

    @Test
    public void duplicateDateTimeCreate() {
        assertThrows(DataAccessException.class, () ->
                service.create(new Meal(userMeal1.getDateTime(), "Duplicate dateTime for user", 100),
                        UserTestData.USER_ID));
    }


    @Test
    public void duplicateDateTimeAnotherUserCreate() {
        assertNotNull(service.create(new Meal(userMeal1.getDateTime(), "Duplicate dateTime, but for admin", 100),
                UserTestData.ADMIN_ID));
    }
}