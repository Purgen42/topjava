package ru.javawebinar.topjava.repository.inmemory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.util.MealsUtil;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class InMemoryMealRepository implements MealRepository {
    private static final Logger log = LoggerFactory.getLogger(InMemoryMealRepository.class);
    private final Map<Integer, Meal> repository = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);
    // Key: Meal.id, Value: User.id
    private final Map<Integer, Integer> userMapping = new ConcurrentHashMap<>();

    {
        MealsUtil.meals.forEach(m -> this.save(m, 1));
        save(new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 3, 0),
                "Some admin food", 5000), 2);
    }

    @Override
    public synchronized Meal save(Meal meal, int userId) {
        log.info("save {}", meal);
        if (meal.isNew()) {
            meal.setId(counter.incrementAndGet());
            repository.put(meal.getId(), meal);
            userMapping.put(meal.getId(), userId);
            return meal;
        }
        if (!Integer.valueOf(userId).equals(userMapping.get(meal.getId()))) {
            return null;
        }
        return repository.computeIfPresent(meal.getId(), (id, oldMeal) -> meal);
    }

    @Override
    public synchronized boolean delete(int id, int userId) {
        log.info("delete {}", id);
        return userMapping.remove(id, userId) && repository.remove(id) != null;
    }

    @Override
    public synchronized Meal get(int id, int userId) {
        log.info("get {}", id);
        return Integer.valueOf(userId).equals(userMapping.get(id)) ? repository.get(id) : null;
    }

    @Override
    public synchronized List<Meal> getAll(int userId) {
        log.info("getAll");
        return repository.values().stream()
                .filter(m -> Integer.valueOf(userId).equals(userMapping.get(m.getId())))
                .sorted(Comparator.comparing(Meal::getDateTime).reversed())
                .collect(Collectors.toList());
    }
}

