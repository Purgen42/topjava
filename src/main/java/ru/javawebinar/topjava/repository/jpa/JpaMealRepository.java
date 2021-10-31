package ru.javawebinar.topjava.repository.jpa;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.MealRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class JpaMealRepository implements MealRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Meal save(Meal meal, int userId) {
        meal.setUser(em.getReference(User.class, userId));
        if (meal.isNew()) {
            em.persist(meal);
            return meal;
        } else if (get(meal.id(), userId) != null) {
            return em.merge(meal);
        }
        return null;
    }

    @Override
    @Transactional
    public boolean delete(int id, int userId) {
        return em.createQuery("DELETE FROM Meal m WHERE m IN (:meal)")
                .setParameter("meal", get(id, userId))
                .executeUpdate() != 0;
    }

    @Override
    public Meal get(int id, int userId) {
        Meal meal = em.find(Meal.class, id);
        return Optional.ofNullable(meal).map(Meal::getUser).map(User::getId).filter(i -> i.equals(userId)).isPresent() ? meal : null;
    }

    @Override
    public List<Meal> getAll(int userId) {
        return em.createQuery("SELECT m FROM Meal m WHERE m.user.id=:userId ORDER BY m.dateTime DESC", Meal.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public List<Meal> getBetweenHalfOpen(LocalDateTime startDateTime, LocalDateTime endDateTime, int userId) {
        return em.createQuery("SELECT m FROM Meal m WHERE m.user.id = :userId AND m.dateTime >= :startDateTime " +
                        "AND m.dateTime < :endDateTime ORDER BY m.dateTime DESC", Meal.class)
                .setParameter("userId", userId)
                .setParameter("startDateTime", startDateTime)
                .setParameter("endDateTime", endDateTime)
                .getResultList();
    }
}