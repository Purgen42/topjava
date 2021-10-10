package ru.javawebinar.topjava.web;

import org.slf4j.Logger;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.dao.MealDao;
import ru.javawebinar.topjava.model.dao.MealDaoMockImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.slf4j.LoggerFactory.getLogger;

public class MealControllerServlet extends HttpServlet {
    private static final Logger log = getLogger(MealControllerServlet.class);
    private static final MealDao dao = MealDaoMockImpl.getInstance();
    private static final String EDIT_URL = "meal.jsp";
    private static final String LIST_URL = "meals";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        int id;
        try {
            id = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e) {
            id = 0;
        }

        switch (action) {
            case "insert":
                log.debug("create new meal");
                req.getRequestDispatcher(EDIT_URL).forward(req, resp);
                break;
            case "edit":
                log.debug("insert meal id=" + id);
                req.setAttribute("meal", dao.getId(id));
                req.getRequestDispatcher(EDIT_URL).forward(req, resp);
                break;
            case "delete":
                log.debug("delete meal id=" + id);
                dao.delete(id);
                resp.sendRedirect(LIST_URL);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        int id;
        LocalDateTime dateTime;
        String description;
        int calories;

        try {
            id = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e) {
            id = 0;
        }
        try {
            dateTime = LocalDateTime.parse(req.getParameter("daytime"));
        } catch (DateTimeParseException e) {
            dateTime = LocalDateTime.MIN;
        }
        description = req.getParameter("description");
        try {
            calories = Integer.parseInt(req.getParameter("calories"));
        } catch (NumberFormatException e) {
            calories = 0;
        }

        log.debug(String.format("save meal id=%d dateTime=%s description=%s calories=%d", id, dateTime, description, calories));
        Meal meal = new Meal(id, dateTime, description, calories);
        if (id == 0) {
            dao.add(meal);
        } else {
            dao.update(id, meal);
        }
        resp.sendRedirect(LIST_URL);
    }
}
