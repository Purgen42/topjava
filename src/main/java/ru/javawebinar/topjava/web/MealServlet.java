package ru.javawebinar.topjava.web;

import org.slf4j.Logger;
import ru.javawebinar.topjava.dao.MealDao;
import ru.javawebinar.topjava.dao.MockMealDao;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.MealTo;
import ru.javawebinar.topjava.util.MealsUtil;
import ru.javawebinar.topjava.util.TimeUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class MealServlet extends HttpServlet {
    private static final Logger log = getLogger(MealServlet.class);
    private final MealDao dao = new MockMealDao();
    private final int CALORIES_PER_DAY = 2000;
    private final String EDIT_JSP = "meal.jsp";
    private final String LIST_URL = "meals";
    private final String LIST_JSP = "meals.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "";
        }
        int id;

        switch (action) {
            case "insert":
                log.debug("create new meal");
                Meal dummyMeal = new Meal(null, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), "", 0);
                req.setAttribute("meal", dummyMeal);
                req.getRequestDispatcher(EDIT_JSP).forward(req, resp);
                break;
            case "edit":
                id = Integer.parseInt(req.getParameter("id"));
                log.debug("edit meal id={}", id);
                req.setAttribute("meal", dao.getById(id));
                req.getRequestDispatcher(EDIT_JSP).forward(req, resp);
                break;
            case "delete":
                id = Integer.parseInt(req.getParameter("id"));
                log.debug("delete meal id={}", id);
                dao.delete(id);
                resp.sendRedirect(LIST_URL);
                break;
            case "cancel":
                resp.sendRedirect(LIST_URL);
                break;
            default:
                log.debug("display meals list");
                List<MealTo> meals = MealsUtil.filteredByStreams(dao.getAll(), TimeUtil.allEntries(), CALORIES_PER_DAY);
                req.setAttribute("meals", meals);
                req.getRequestDispatcher(LIST_JSP).forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String idString = req.getParameter("id");
        Integer id = idString.isEmpty() ? null : Integer.parseInt(idString);
        LocalDateTime dateTime = LocalDateTime.parse(req.getParameter("daytime"));
        String description = req.getParameter("description");
        int calories = Integer.parseInt(req.getParameter("calories"));

        Meal meal = new Meal(id, dateTime, description, calories);
        if (id == null) {
            meal = dao.add(meal);
        } else {
            meal = dao.update(meal);
        }
        log.debug("save meal id={} dateTime={} description={} calories={}", meal.getId(), dateTime, description, calories);
        resp.sendRedirect(LIST_URL);
    }
}
