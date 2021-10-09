<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Meals list</title>
    <style>
        table, th, td {
            border: 1px solid black;
            border-collapse: collapse;
        }
        td {
            color: white;
        }
        table {
            width: 100%;
        }
        .notExceed {
            background-color: green;
        }
        .exceed {
            background-color: red;
        }

    </style>
</head>
<body>
<table>
    <caption><H1>Meals table</H1></caption>
    <colgroup>
        <col span="1" style="width: 30%;">
        <col span="1" style="width: 60%;">
        <col span="1" style="width: 10%;">
    </colgroup>
    <tr>
        <th>Date and time</th>
        <th>Description</th>
        <th>Calories</th>
    </tr>
<c:forEach items="${requestScope.meals}" var="meal">
    <tr class="${meal.excess ? 'exceed':'notExceed'}">
        <td><c:out value="${meal.dateTimeString}"/></td>
        <td><c:out value="${meal.description}"/></td>
        <td><c:out value="${meal.calories}"/></td>
    </tr>
</c:forEach>
</table>
</body>
</html>
