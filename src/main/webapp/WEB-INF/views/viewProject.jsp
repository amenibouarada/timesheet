<%@ page import="com.aplana.timesheet.dao.entity.Division" %>
<%@ page import="java.util.Set" %>
<%@ page import="com.aplana.timesheet.dao.entity.Project" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/resources/css/adminProjectEdit.css">
    <title>Просмотр данных проекта</title>
</head>
<body>
<h1>
    Просмотр данных проекта
</h1>
<br/>
<table class="maintable" style="margin-bottom: 20px;">
    <tr style="visibility: collapse">
        <th width="200">1</th>
        <th width="300">2</th>
        <th width="300">3</th>
        <th>4</th>
    </tr>
    <tr>
        <td>
            <span class="lowspace">Полное название:</span>
        </td>
        <td colspan="2">
            ${project.name}
        </td>
    </tr>
    <tr>
        <td>
            <span class="lowspace">Описание:</span>
        </td>
        <td colspan="2">
            ${project.description}
        </td>
    </tr>
    <tr>
        <td>
            <span class="lowspace">Заказчик:</span>
        </td>
        <td colspan="2">
            ${project.customer}
        </td>
    </tr>
    <tr>
        <td>
            <span class="lowspace">Тип финансирования:</span>
        </td>
        <td>
            ${project.fundingType.value}
        </td>
    </tr>
    <tr>
        <td>
            <span class="lowspace ">Руководитель проекта:</span>
        </td>
        <td>
            ${project.manager != null ? project.manager.name : ""}
        </td>
    </tr>
    <tr>
        <td>
            <span class="lowspace">Команда</span>
        </td>
        <td colspan="4">
            <table class="details_table" id="projectManagers">
                <tr>
                    <th width="250">Сотрудник</th>
                    <th width="250">Роль</th>
                    <th width="100">Главный</th>

                </tr>
                <c:forEach items="${projectManagers}" varStatus="row" var="manager">
                    <tr id="projectManager_${row.index}" class="manager_row">
                        <td>
                            <c:out value="${manager.employee.name}"/>
                        </td>
                        <td>
                            <c:out value="${manager.projectRole.name}"/>
                        </td>
                        <td>
                            <input id="managerMaster" name="managerMaster" type="checkbox"
                                   checked="<c:out value="${manager.master}"/>" disabled="true">
                        </td>

                    </tr>
                </c:forEach>
            </table>
        </td>
    </tr>
    <tr>
        <td>
            <span class="lowspace">Технологии:</span>
        </td>
        <td colspan="2">
            <textarea rows="3" class="show_border" disabled="true">${project.passport}</textarea>
        </td>
    </tr>
    <tr>
        <td>
            <span class="lowspace">Дата начала:</span>
        </td>
        <td>
            <div class="horizontal_block">
                <fmt:formatDate value="${project.startDate}" pattern="dd.MM.yyyy"/>
            </div>
        </td>
    </tr>
    <tr>
        <td>
            <span class="lowspace">Дата окончания:</span>
        </td>
        <td>
            <div class="horizontal_block">
                <fmt:formatDate value="${project.endDate}" pattern="dd.MM.yyyy"/>
            </div>
        </td>
    </tr>
</table>
</body>
</html>