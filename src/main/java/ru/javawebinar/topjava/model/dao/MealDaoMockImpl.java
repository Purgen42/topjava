package ru.javawebinar.topjava.model.dao;

import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.MealsTestValues;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MealDaoMockImpl implements MealDao {
    private final Map<Integer, Meal> mockDataSource;
    private final AtomicInteger counter;
    private final ReadWriteLock lock;

    private MealDaoMockImpl() {
        mockDataSource = new TreeMap<>();
        counter = new AtomicInteger(1);
        lock = new ReentrantReadWriteLock();
        MealsTestValues.TEST_MEALS_LIST.forEach(this::add);
    }

    private static class LazyHolder {
        public static final MealDaoMockImpl INSTANCE = new MealDaoMockImpl();
    }

    public static MealDaoMockImpl getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public void add(Meal meal) {
        int id = counter.getAndIncrement();
        update(id, meal);
    }

    @Override
    public List<Meal> getAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(mockDataSource.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Meal getId(int id) {
        lock.readLock().lock();
        try {
            return mockDataSource.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void update(int id, Meal meal) {
        lock.writeLock().lock();
        try {
            Meal newMeal = new Meal(id, meal.getDateTime(), meal.getDescription(), meal.getCalories());
            mockDataSource.put(id, newMeal);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(int id) {
        lock.writeLock().lock();
        try {
            mockDataSource.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
