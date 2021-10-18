package ru.javawebinar.topjava.repository.inmemory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.util.DateTimeUtil;
import ru.javawebinar.topjava.util.MealsUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class InMemoryMealRepository implements MealRepository {
    private static final Logger log = LoggerFactory.getLogger(InMemoryMealRepository.class);
    //                <User.id, <Meal.id, Meal>>
    private final Map<Integer, Map<Integer, Meal>> repository = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    {
        MealsUtil.meals.forEach(m -> this.save(m, 1));
        save(new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 3, 0),
                "Some admin food", 5000), 2);
    }

    @Override
    public Meal save(Meal meal, int userId) {
        log.info("save {}", meal);
        if (meal.isNew()) {
            meal.setId(counter.incrementAndGet());
            repository.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
            repository.get(userId).put(meal.getId(), meal);
            return meal;
        }
        return Optional.ofNullable(repository.get(userId))
                .map(m -> m.computeIfPresent(meal.getId(), (id, oldMeal) -> meal)).orElse(null);
    }

    @Override
    public boolean delete(int id, int userId) {
        log.info("delete {}", id);
        return Optional.ofNullable(repository.get(userId)).map(m -> m.remove(id)).isPresent();
    }

    @Override
    public Meal get(int id, int userId) {
        log.info("get {}", id);
        return Optional.ofNullable(repository.get(userId)).map(m -> m.get(id)).orElse(null);
    }

    @Override
    public List<Meal> getAll(int userId) {
        log.info("getAll");
        return filterByPredicate(userId, m -> true);
    }

    @Override
    public List<Meal> getFiltered(LocalDate startDate, LocalDate endDate, int userId) {
        return filterByPredicate(userId, m -> DateTimeUtil.isBetweenClosed(m.getDate(), startDate, endDate));
    }

    private List<Meal> filterByPredicate(int userId, Predicate<Meal> predicate) {
        return Optional.ofNullable(repository.get(userId)).map(m -> m.values().stream()).orElse(Stream.empty())
                .filter(predicate)
                .sorted(Comparator.comparing(Meal::getDateTime).reversed())
                .collect(Collectors.toList());
    }
}