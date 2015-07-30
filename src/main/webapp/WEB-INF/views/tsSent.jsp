<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="title.timesheet"/></title>
    <script type="text/javascript">
        function openViewReportsWindow() {
            var employeeId = dojo.byId("employeeId").value;
            if (employeeId != 0) {
                var date = new Date();
                window.open('viewreports/' + employeeId + '/' + date.getFullYear() + '/' + (date.getMonth() + 1), 'reports_window' + employeeId);
            }
        }
    </script>
</head>
<body>
<br/>
<%--ToDo переименовать--%>
<form:form method="post" action="sendNewReport" commandName="timeSheetForm">
    <form:hidden path="employeeId" id="employeeId"/>
    <form:hidden path="divisionId" id="divisionId"/>
    <div><b>${message}</b></div>
    <br>
    <button id="submit_button" type="submit">Отправить новый отчёт</button>
    <button id="view_reports_button" type="button" onclick="openViewReportsWindow()">Просмотр отчетов</button>
</form:form>
</body>
</html>