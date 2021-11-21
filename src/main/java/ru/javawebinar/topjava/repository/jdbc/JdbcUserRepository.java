package ru.javawebinar.topjava.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
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

import javax.validation.Validation;
import javax.validation.Validator;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Transactional(readOnly = true)
public class JdbcUserRepository implements UserRepository {

    private final Validator validator;

    private static final RowMapper<User> ROW_MAPPER_WITH_ROLES = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setCaloriesPerDay(rs.getInt("calories_per_day"));
        user.setEnabled(rs.getBoolean("enabled"));
        user.setRegistered(rs.getDate("registered"));
        ResultSet rolesResultSet = rs.getArray("roles").getResultSet();
        Set <Role> roles = new HashSet<>();
        while(rolesResultSet.next()) {
            roles.add(Role.valueOf(rolesResultSet.getString(2)));
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
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Override
    @Transactional
    public User save(User user) {
        if (!validator.validate(user).isEmpty()) {
            return null;
        }

        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(user);

        if (user.isNew()) {
            Number newKey = insertUser.executeAndReturnKey(parameterSource);
            user.setId(newKey.intValue());
            batchInsert(user);
        } else if (namedParameterJdbcTemplate.update("""
                   UPDATE users SET name=:name, email=:email, password=:password, 
                   registered=:registered, enabled=:enabled, calories_per_day=:caloriesPerDay WHERE id=:id
                """, parameterSource) == 0) {
            return null;
        } else {
            jdbcTemplate.update("DELETE FROM user_roles WHERE user_id=?", user.id());
            batchInsert(user);
        }
        return user;
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id=?; ", id) != 0;
    }

    @Override
    public User get(int id) {
//        jdbcTemplate.query("SELECT * FROM users WHERE id=?", ROW_MAPPER, id);
        List<User> users = jdbcTemplate.query("SELECT u.id, u.name, u.email, u.password, u.registered, u.enabled, " +
                "u.calories_per_day, ARRAY_AGG(ur.role) as roles FROM users u LEFT JOIN user_roles ur ON u.id = ur.user_id  WHERE u.id=? " +
                "GROUP BY u.id, u.name, u.email, u.password, u.registered, u.enabled, u.calories_per_day ORDER BY name, email",
                ROW_MAPPER_WITH_ROLES, id);
        return DataAccessUtils.singleResult(users);
    }

    @Override
    public User getByEmail(String email) {
//        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE email=?", ROW_MAPPER, email);
//        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE email=?", ROW_MAPPER, email);
        List<User> users = jdbcTemplate.query("SELECT u.id, u.name, u.email, u.password, u.registered, u.enabled, " +
                        "u.calories_per_day, ARRAY_AGG(ur.role) as roles FROM users u LEFT JOIN user_roles ur ON u.id = ur.user_id  WHERE email=? " +
                        "GROUP BY u.id, u.name, u.email, u.password, u.registered, u.enabled, u.calories_per_day ORDER BY name, email",
                ROW_MAPPER_WITH_ROLES, email);
        return DataAccessUtils.singleResult(users);
    }

    @Override
    public List<User> getAll() {
        return jdbcTemplate.query("SELECT u.id, u.name, u.email, u.password, u.registered, u.enabled, " +
                "u.calories_per_day, ARRAY_AGG(ur.role) as roles FROM users u LEFT JOIN user_roles ur ON u.id = ur.user_id " +
                "GROUP BY u.id, u.name, u.email, u.password, u.registered, u.enabled, u.calories_per_day ORDER BY name, email",
                ROW_MAPPER_WITH_ROLES);
    }

    private void batchInsert(User user) {
        Role[] roleArray = user.getRoles().toArray(new Role[0]);
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
