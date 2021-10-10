<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://topjava.ru/functions" prefix="f" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Meal</title>
    <style>
        form {
            display: table;
        }

        p {
            display: table-row;
        }

        label {
            display: table-cell;
        }

        input {
            display: table-cell;
        }
    </style>
</head>
<body>
<c:set var="meal" value="${requestScope.meal}"/>
<form method="POST" action="mealController" name="frmAddMeal">
    <input type="hidden" id="id" name="id" value="${meal.id}"/>
    <p>
        <label for="daytime">Date/Time: </label>
        <input type="datetime-local" id="daytime" name="daytime" value="${meal.dateTime}"/>
    </p>
    <p>
        <label for="description">Description: </label>
        <input type="text" id="description" name="description" value="${meal.description}"/>
    </p>
    <p>
        <label for="calories">Calories: </label>
        <input type="number" id="calories" name="calories" min="0" value="${meal.calories}"/>
    </p>
    <input type="submit" value="Save"/>
    <input type="submit" name="cancel" value="Cancel" formaction="meals"/>
</form>
</body>
</html>
