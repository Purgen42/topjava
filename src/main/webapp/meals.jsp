<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://topjava.ru/functions" prefix="f" %>
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

        .linkColumn {
            text-align: center;
        }

        .tableLink {
            color: yellow;
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
        <col span="1" style="width: 20%;">
        <col span="1" style="width: 50%;">
        <col span="1" style="width: 10%;">
        <col span="1" style="width: 10%;">
        <col span="1" style="width: 10%;">
    </colgroup>
    <tr>
        <th>Date and time</th>
        <th>Description</th>
        <th>Calories</th>
        <th></th>
        <th></th>
    </tr>
    <c:forEach items="${requestScope.meals}" var="meal">
        <tr class="${meal.excess ? "exceed":"notExceed"}">
            <td><c:out value="${f:formatLocalDateTime(meal.dateTime, initParam.dateTimePattern)}"/></td>
            <td><c:out value="${meal.description}"/></td>
            <td><c:out value="${meal.calories}"/></td>
            <td class="linkColumn"><a class="tableLink"
                                      href="mealController?action=edit&id=<c:out value="${meal.id}"/>">Update</a></td>
            <td class="linkColumn"><a class="tableLink"
                                      href="mealController?action=delete&id=<c:out value="${meal.id}"/>">Delete</a></td>
        </tr>
    </c:forEach>
</table>
<p><a href="mealController?action=insert">Add User</a></p>
</body>
</html>
