<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Meal</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
<h3><a href="index.html">Home</a></h3>
<c:set var="meal" value="${requestScope.meal}"/>
<h1>${meal.id == null ? "Add":"Edit"} meal</h1>
<form method="POST" action="meals" name="frmAddMeal">
    <input type="hidden" id="id" name="id" value="${meal.id}"/>
    <input type="hidden" id="action" name="action" value="cancel"/>
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
    <input type="submit" name="cancel" value="Cancel" formmethod="GET"/>
</form>
</body>
</html>
