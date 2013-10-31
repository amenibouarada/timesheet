<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="title.timesheet"/></title>
</head>

<body>

<br/>
<form:form method="post" action="sendNewReport" commandName="timeSheetForm">
    <form:hidden path="employeeId" id="employeeId"/>
    <form:hidden path="divisionId" id="divisionId"/>
    <div><b>Отчет успешно отправлен!</b></div>
    <br/>
    <button id="submit_button" type="submit">Отправить новый отчёт</button>
    <button id="view_reports_button" type="button" onclick="openViewReportsWindow()">Просмотр отчетов</button>
</form:form>

</body>
</html>