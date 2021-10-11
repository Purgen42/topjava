<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://topjava.ru/functions" prefix="f" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Meals list</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
<h3><a href="index.html">Home</a></h3>
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
            <td>${f:formatLocalDateTime(meal.dateTime)}</td>
            <td>${meal.description}</td>
            <td>${meal.calories}</td>
            <td class="linkColumn"><a class="tableLink"
                                      href="meals?action=edit&id=${meal.id}">Update</a></td>
            <td class="linkColumn"><a class="tableLink"
                                      href="meals?action=delete&id=${meal.id}">Delete</a></td>
        </tr>
    </c:forEach>
</table>
<p><a href="meals?action=insert">Add Meal</a></p>
</body>
</html>
