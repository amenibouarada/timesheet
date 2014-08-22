<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="adminprojects.${pageFunction}"/></title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/resources/css/adminProjectEdit.css">
    <script type="text/javascript">
        var divisionsEmployeesJSON = ${divisionsEmployeesJSON};
        var employeesListJSON = ${employeesListJSON};
        var projectRoleTypesJSON = ${projectRoleTypesJSON};
        var managerId = "${managerId}";
    </script>
    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/adminProjectEdit.js"></script>
</head>
<body>
<h1>
    <fmt:message key="adminprojects.${pageFunction}"/>
</h1>
<br/>
<form:form method="post" commandName="projectform" name="projectform">
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
        <form:input path="name"/>
    </td>
</tr>
<tr>
    <td>
        <span class="lowspace">Центр компетенции:</span>
    </td>
    <td>
        <form:select path="division" id="divisionId">
            <form:option label="Не проставлено (null)" value="0"/>
            <form:options items="${divisionsList}" itemLabel="name" itemValue="id"/>
        </form:select>
    </td>
</tr>
<tr>
    <td>
        <span class="lowspace">Центр руководителя:</span>
    </td>
    <td>
        <form:select path="managerDivision" id="managerDivisionId"
                     onchange="updateManagerSelect(this.value);">
            <form:options items="${divisionsList}" itemLabel="name" itemValue="id"/>
        </form:select>
    </td>
</tr>
<tr>
    <td>
        <span class="lowspace ">Руководитель:</span>
    </td>
    <td>
        <div id="managerId" name="manager"></div>
    </td>
</tr>
<tr>
    <td>
        <span class="lowspace">Заказчик:</span>
    </td>
    <td colspan="2">
        <form:input path="customer"/>
    </td>
</tr>
<tr>
    <td>
        <span class="lowspace">Дата начала:</span>
    </td>
    <td>
        <div class="horizontal_block">
            <form:input path="startDate" id="startDate" class="date_picker"
                        data-dojo-type="DateTextBox" required="true"/>
        </div>
    </td>
</tr>
<tr>
    <td>
        <span class="lowspace">Дата окончания:</span>
    </td>
    <td>
        <div class="horizontal_block">
            <form:input path="endDate" id="endDate" class="date_picker"
                        data-dojo-type="DateTextBox" required="true"/>
        </div>
    </td>
</tr>
<tr>
    <td>
        <span class="lowspace">Тип проекта:</span>
    </td>
    <td>
        <form:select path="state" id="stateId">
            <form:options items="${projectStateTypes}" itemLabel="name" itemValue="id"/>
        </form:select>
    </td>
</tr>
<tr>
    <td>
        <span class="lowspace">Тип финансирования:</span>
    </td>
    <td>
        <form:select path="fundingType" id="fundingTypeId">
            <form:options items="${projectFundingTypes}" itemLabel="name" itemValue="id"/>
        </form:select>
    </td>
</tr>
<tr>
    <td>
        <span class="lowspace">Имя в Jira:</span>
    </td>
    <td>
        <form:input path="jiraKey"/>
    </td>
</tr>
<tr>
    <td>
        <span class="lowspace">Признак активности:</span>
    </td>
    <td>
        <form:checkbox path="active"/>
    </td>
</tr>
<tr>
    <td>
        <span class="lowspace">Требование указания задачи:</span>
    </td>
    <td>
        <form:checkbox path="cqRequired"/>
    </td>
</tr>
<tr>
    <td>
        <span class="lowspace">Задачи:</span>
    </td>
    <td colspan="4">
        <table class="details_table" id="projectTasks">
            <tr>
                <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
                    <th width="32">
                        <img class="iconbutton" title="Создать"
                             src="<c:url value="/resources/img/add.gif"/>"
                             onclick="createTask();"/>
                    </th>
                </sec:authorize>
                <th width="207">Наименование задачи</th>
                <th width="207">Описание</th>
                <th width="100">Признак активности</th>
                <th width="100">Приоритет</th>
            </tr>
            <c:forEach items="${projectform.projectTasks}" varStatus="row">
                <tr id="projectTask_${row.index}" class="task_row">
                    <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
                        <td>
                            <img class="iconbutton hidden_button" title="Удалить" id="taskDeleteButton_${row.index}"
                                 src="<c:url value="/resources/img/delete.png"/>"
                                 onclick="deleteTask(${row.index});"/>
                        </td>
                    </sec:authorize>
                    <td class="multiline">
                        <form:hidden path="projectTasks[${row.index}].id"/>
                        <form:hidden path="projectTasks[${row.index}].toDelete"/>
                        <form:textarea path="projectTasks[${row.index}].name" cssClass="multiline task_name" rows="3"/>
                    </td>
                    <td class="multiline">
                        <form:textarea path="projectTasks[${row.index}].description"
                                       cssClass="multiline task_description" rows="3"/>
                    </td>
                    <td>
                        <form:checkbox path="projectTasks[${row.index}].active"/>
                    </td>
                    <td>
                        <form:textarea path="projectTasks[${row.index}].priority"
                                       cssClass="multiline" rows="3"/>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </td>
</tr>
<tr>
    <td>
        <span class="lowspace">Проектные роли:</span>
    </td>
    <td colspan="4">
        <table class="details_table" id="projectManagers">
            <tr>
                <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
                    <th width="32">
                        <img class="iconbutton" title="Создать"
                             src="<c:url value="/resources/img/add.gif"/>"
                             onclick="createManager();"/>
                    </th>
                </sec:authorize>
                <th width="250">Сотрудник</th>
                <th width="250">Роль</th>
                <th width="100">Главный</th>
                <th width="100">Признак активности</th>
            </tr>
            <c:forEach items="${projectform.projectManagers}" varStatus="row" var="projectManager">
                <tr id="projectManager_${row.index}" class="manager_row">
                    <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
                        <td>
                            <img class="iconbutton hidden_button" title="Удалить" id="managerDeleteButton_${row.index}"
                                 src="<c:url value="/resources/img/delete.png"/>"
                                 onclick="deleteManager(${row.index});"/>
                        </td>
                    </sec:authorize>
                    <td>
                        <form:hidden path="projectManagers[${row.index}].id"/>
                        <form:hidden path="projectManagers[${row.index}].toDelete"/>
                        <script>
                            dojo.ready(function () {
                                createFilteringSelect(${row.index}, ${projectManager.employee}, projectRolesFieldNames, "projectManager_");
                            });
                        </script>

                    </td>
                    <td>
                        <form:select path="projectManagers[${row.index}].projectRole">
                            <form:options items="${projectRoleTypes}" itemLabel="name" itemValue="id"/>
                        </form:select>
                    </td>
                    <td>
                        <form:checkbox path="projectManagers[${row.index}].master"/>
                    </td>
                    <td>
                        <form:checkbox path="projectManagers[${row.index}].active"/>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </td>
</tr>
<tr>
    <td>
        <span class="lowspace">Учитывать в затратах:</span>
    </td>
    <td colspan="4">
        <table class="details_table" id="projectBillables">
            <tr>
                <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
                    <th width="32">
                        <img class="iconbutton" title="Создать"
                             src="<c:url value="/resources/img/add.gif"/>"
                             onclick="createBillable();"/>
                    </th>
                </sec:authorize>
                <th width="200">Сотрудник</th>
                <th width="100">Учитывать в затратах</th>
                <th width="150">Дата с</th>
                <th width="150">Дата по</th>
                <th width="300">Основание</th>
            </tr>
            <c:forEach items="${projectform.projectBillables}" varStatus="row" var="projectBillable">
                <tr id="projectBillable_${row.index}" class="billable_row">
                    <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
                        <td>
                            <img class="iconbutton" title="Удалить" id="billableDeleteButton_${row.index}"
                                 src="<c:url value="/resources/img/delete.png"/>"
                                 onclick="deleteBillable(${row.index});"/>
                        </td>
                    </sec:authorize>
                    <td>
                        <form:hidden path="projectBillables[${row.index}].id"/>
                        <form:hidden path="projectBillables[${row.index}].toDelete"/>
                        <script>
                            dojo.ready(function () {
                                createFilteringSelect(${row.index}, ${projectBillable.employee}, billableFieldNames, "projectBillable_");
                            });
                        </script>
                    </td>
                    <td>
                        <form:checkbox path="projectBillables[${row.index}].billable"/>
                    </td>
                    <td>
                        <form:input path="projectBillables[${row.index}].startDate"
                                    data-dojo-type="DateTextBox" cssClass="billable_date"/>
                    </td>
                    <td>
                        <form:input path="projectBillables[${row.index}].endDate"
                                    data-dojo-type="DateTextBox" cssClass="billable_date"/>
                    </td>
                    <td>
                        <form:textarea path="projectBillables[${row.index}].comment" rows="3"/>
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
        <form:textarea path="passport" rows="3" cssClass="show_border"/>
    </td>
</tr>
<tr>
    <td>
                <span class="lowspace">Центры, у которых<br/>
                    есть возможность списывать<br/>
                    занятость по проекту:</span>
    </td>
    <td>
        <form:select multiple="multiple" path="projectDivisions" rows="3">
            <form:options items="${divisionsList}" itemLabel="name" itemValue="id"/>
        </form:select>
    </td>
</tr>
<tr style="visibility: collapse">
    <td>
        <span class="lowspace">Идентификатор проекта:</span>
    </td>
    <td colspan="2">
        <form:input path="id"/>
    </td>
</tr>
<tr>
    <td>
        <button id="saveButton" style="width: 200px" onclick="saveProject()" type="button">
            Сохранить
        </button>
    </td>
    <td>
        <button id="cancelButton" style="width: 200px"
                onclick="location.href='<%= request.getContextPath()%>/admin/projects'" type="button">
            Отмена
        </button>
    </td>
</tr>
</table>
</form:form>
</body>
</html>
