package ru.javawebinar.topjava.repository.datajpa;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class DataJpaMealRepository implements MealRepository {

    private static final Sort SORT_DATETIME = Sort.by(Sort.Direction.DESC, "dateTime");

    private final CrudMealRepository crudMealRepository;
    private final CrudUserRepository crudUserRepository;

    public DataJpaMealRepository(CrudMealRepository crudMealRepository, CrudUserRepository crudUserRepository) {
        this.crudMealRepository = crudMealRepository;
        this.crudUserRepository = crudUserRepository;
    }

    @Override
    @Transactional
    public Meal save(Meal meal, int userId) {
        meal.setUser(crudUserRepository.getById(userId));
        return (meal.isNew() || get(meal.getId(), userId) != null) ? crudMealRepository.save(meal) : null;
    }

    @Override
    public boolean delete(int id, int userId) {
        return crudMealRepository.delete(id, userId) != 0;
    }

    @Override
    public Meal get(int id, int userId) {
        return crudMealRepository.findOne(getByUserSpecification(userId).and(getByIdSpecification(id))).orElse(null);
    }

    @Override
    public Meal getWithUser(int id, int userId) {
        return crudMealRepository.getJoinUser(id, userId);
    }

    @Override
    public List<Meal> getAll(int userId) {
        return crudMealRepository.findAll(getByUserSpecification(userId), SORT_DATETIME);
    }

    @Override
    public List<Meal> getBetweenHalfOpen(LocalDateTime startDateTime, LocalDateTime endDateTime, int userId) {
        return crudMealRepository.findAll(getByUserSpecification(userId).and(getByDateTimeSpecification(startDateTime, endDateTime)),
                SORT_DATETIME);
    }

    private Specification<Meal> getByIdSpecification(int id) {
        return (root, query, builder) -> builder.equal(root.get("id"), id);
    }

    private Specification<Meal> getByUserSpecification(int userId) {
        return (root, query, builder) -> builder.equal(root.get("user").get("id"), userId);
    }

    private Specification<Meal> getByDateTimeSpecification(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return (root, query, builder) -> builder.and(builder.greaterThanOrEqualTo(root.get("dateTime"), startDateTime),
                builder.lessThan(root.get("dateTime"), endDateTime));
    }
}