package ru.javawebinar.topjava.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.javawebinar.topjava.model.Role;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.UserRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ru.javawebinar.topjava.util.ValidationUtil.validateEntity;

@Repository
@Transactional(readOnly = true)
public class JdbcUserRepository implements UserRepository {

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> {
        User user = new BeanPropertyRowMapper<>(User.class).mapRow(rs, rowNum);
        ResultSet rolesResultSet = rs.getArray("roles_array").getResultSet();
        Set <Role> roles = new HashSet<>();
        while(rolesResultSet.next()) {
            String roleName = rolesResultSet.getString(2);
            if (roleName != null) {
                roles.add(Role.valueOf(roleName));
            }
        }
        user.setRoles(roles);
        return user;
    };

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final SimpleJdbcInsert insertUser;

    @Autowired
    public JdbcUserRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.insertUser = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    @Transactional
    public User save(User user) {
        validateEntity(user);

        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(user);

        if (user.isNew()) {
            Number newKey = insertUser.executeAndReturnKey(parameterSource);
            user.setId(newKey.intValue());
        } else if (namedParameterJdbcTemplate.update("""
                   UPDATE users SET name=:name, email=:email, password=:password, 
                   registered=:registered, enabled=:enabled, calories_per_day=:caloriesPerDay WHERE id=:id
                """, parameterSource) == 0) {
            return null;
        } else {
            jdbcTemplate.update("DELETE FROM user_roles WHERE user_id=?", user.id());
        }
        insertRoles(user);
        return user;
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id=?", id) != 0;
    }

    @Override
    public User get(int id) {
//        jdbcTemplate.query("SELECT * FROM users WHERE id=?", ROW_MAPPER, id);
        List<User> users = jdbcTemplate.query("""
                        SELECT u.id, u.name, u.email, u.password, u.registered, u.enabled, 
                        u.calories_per_day, ARRAY_AGG(ur.role) as roles_array FROM users u LEFT JOIN user_roles ur ON u.id = ur.user_id  WHERE u.id=? 
                        GROUP BY u.id, u.name, u.email, u.password, u.registered, u.enabled, u.calories_per_day ORDER BY name, email
                      """, ROW_MAPPER, id);
        return DataAccessUtils.singleResult(users);
    }

    @Override
    public User getByEmail(String email) {
//        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE email=?", ROW_MAPPER, email);
//        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE email=?", ROW_MAPPER, email);
        List<User> users = jdbcTemplate.query("""
                        SELECT u.id, u.name, u.email, u.password, u.registered, u.enabled, 
                        u.calories_per_day, ARRAY_AGG(ur.role) as roles_array FROM users u LEFT JOIN user_roles ur ON u.id = ur.user_id  WHERE email=? 
                        GROUP BY u.id, u.name, u.email, u.password, u.registered, u.enabled, u.calories_per_day ORDER BY name, email
                      """, ROW_MAPPER, email);
        return DataAccessUtils.singleResult(users);
    }

    @Override
    public List<User> getAll() {
        return jdbcTemplate.query("""
                        SELECT u.id, u.name, u.email, u.password, u.registered, u.enabled, 
                        u.calories_per_day, ARRAY_AGG(ur.role) as roles_array FROM users u LEFT JOIN user_roles ur ON u.id = ur.user_id 
                        GROUP BY u.id, u.name, u.email, u.password, u.registered, u.enabled, u.calories_per_day ORDER BY name, email
                      """, ROW_MAPPER);
    }

    private void insertRoles(User user) {
        Role[] roleArray = user.getRoles().toArray(new Role[0]);
        if (roleArray.length == 0) {
            return;
        }
        jdbcTemplate.batchUpdate("INSERT INTO user_roles (user_id, role) VALUES (?,?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, user.id());
                        ps.setString(2, roleArray[i].name());
                    }

                    @Override
                    public int getBatchSize() {
                        return roleArray.length;
                    }
                });
    }
}
