package ru.javawebinar.topjava.repository.datajpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.javawebinar.topjava.model.Meal;

@Transactional(readOnly = true)
public interface CrudMealRepository extends JpaRepository<Meal, Integer>, JpaSpecificationExecutor<Meal> {

    @Transactional
    @Modifying
    @Query("DELETE FROM Meal m WHERE m.id=:id AND m.user.id=:userId")
    int delete(@Param("id") int id, @Param("userId") int userId);

    @Query("SELECT m FROM Meal m LEFT JOIN FETCH m.user u WHERE m.id=:id AND m.user.id=:userId")
    Meal getJoinUser(@Param("id") int id, @Param("userId") int userId);
}
