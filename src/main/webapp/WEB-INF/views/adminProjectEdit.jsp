<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title></title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/resources/css/adminProjectEdit.css">
    <script type="text/javascript">
        dojo.declare("DateTextBox", dijit.form.DateTextBox, {
            popupClass:"dijit.Calendar",
            datePattern: 'dd.MM.yyyy'
        });

        function saveProject() {
            //projectform.action = "save";
            projectform.submit();
        }

        function createTask() {
            var projectTasks = dojo.byId("projectTasks");
            var projectTasksRows = dojo.query(".task_row");
            var tasksCount = projectTasksRows.length;

            var newTask = projectTasks.insertRow(tasksCount + 1);
            dojo.addClass(newTask, "task_row");

            var newTaskIndex;
            if (tasksCount == 0) {
                newTaskIndex = 0;
            }
            else {
                var lastTask = projectTasksRows[tasksCount - 1];
                var lastTaskId = dojo.attr(lastTask, "id");
                var lastTaskIndex = parseInt(lastTaskId.substring(lastTaskId.lastIndexOf("_") + 1, lastTaskId.length));
                newTaskIndex = lastTaskIndex + 1;
            }
            dojo.attr(newTask, {id : "projectTask_" + newTaskIndex});

            // Кнопка удаления
            var deleteCell = newTask.insertCell(0);
            var img = dojo.doc.createElement("img");
            dojo.attr(img, {
                id:"taskDeleteButton_" + newTaskIndex,
                class: "iconbutton",
                src:"<%= request.getContextPath()%>/resources/img/delete.png",
                alt:"Удалить",
                title:"Удалить"
            });
            /*img.onclick = function () {
                deleteRow(newRowIndex);
            };*/
            deleteCell.appendChild(img);

            // Наименование задачи
            var nameCell = newTask.insertCell(1);
            dojo.addClass(nameCell , "multiline");
            var nameInput = dojo.doc.createElement("textarea");
            dojo.attr(nameInput , {
                id:"taskNameInput_" + newTaskIndex,
                name:"projectTasks[" + newTaskIndex + "].name",
                wrap:"soft",
                rows:"2"
            });
            nameCell.appendChild(nameInput);

            // Описание
            var descriptionCell = newTask.insertCell(2);
            dojo.addClass(descriptionCell , "multiline");
            var descriptionInput = dojo.doc.createElement("textarea");
            dojo.attr(descriptionInput , {
                id:"taskDescriptionInput_" + newTaskIndex,
                name:"projectTasks[" + newTaskIndex + "].description",
                wrap:"soft",
                rows:"2"
            });
            descriptionCell.appendChild(descriptionInput);

            // Признак активности
            var activeCell = newTask.insertCell(3);
            var activeInput = dojo.doc.createElement("input");
            dojo.attr(activeInput , {
                id:"taskActiveInput_" + newTaskIndex,
                name:"projectTasks[" + newTaskIndex + "].active",
                type:"checkbox"
            });
            activeCell.appendChild(activeInput);

            // Приоритет
            var priorityCell = newTask.insertCell(4);
            dojo.addClass(priorityCell , "multiline");
            var priorityInput = dojo.doc.createElement("textarea");
            dojo.attr(priorityInput, {
                id:"taskPriorityInput_" + newTaskIndex,
                name:"projectTasks[" + newTaskIndex + "].priority",
                wrap:"soft",
                rows:"2"
            });
            priorityCell.appendChild(priorityInput);
        }

        function createManager() {
            var projectManagers = dojo.byId("projectManagers");
            var projectManagersRows = dojo.query(".manager_row");
            var managersCount = projectManagersRows.length;

            var newManager = projectManagers.insertRow(managersCount + 1);
            dojo.addClass(newManager, "manager_row");

            var newManagerIndex;
            if (managersCount == 0) {
                newManagerIndex = 0;
            }
            else {
                var lastManager = projectManagersRows[managersCount - 1];
                var lastManagerId = dojo.attr(lastManager, "id");
                var lastManagerIndex = parseInt(lastManagerId.substring(lastManagerId.lastIndexOf("_") + 1, lastManagerId.length));
                newManagerIndex = lastManagerIndex + 1;
            }
            dojo.attr(newManager, {id : "projectManager_" + newManagerIndex});

            // Кнопка удаления
            var deleteCell = newManager.insertCell(0);
            var img = dojo.doc.createElement("img");
            dojo.attr(img, {
                id:"managerDeleteButton_" + newManagerIndex,
                class: "iconbutton",
                src:"<%= request.getContextPath()%>/resources/img/delete.png",
                alt:"Удалить",
                title:"Удалить"
            });
            /*img.onclick = function () {
             deleteRow(newRowIndex);
             };*/
            deleteCell.appendChild(img);

            //TODO Дописать добавление новой строки менеджеров

            // Наименование задачи
            var employeeCell = newManager.insertCell(1);
            var employeeInput = dojo.doc.createElement("input");
            dojo.attr(nameInput , {
                id:"managerEmployeeInput_" + newManagerIndex,
                name:"projectManagers[" + newManagerIndex + "].employee",
                type:"text"
            });
            employeeCell.appendChild(employeeInput);
        }
    </script>
</head>
<body>
<h1><fmt:message key="adminprojects"/></h1>
<br/>
<form:form method="post" commandName="projectform" name="projectform">
    <table class="maintable" style="margin-bottom: 20px;">
        <tr style="visibility: hidden">
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
                <form:select path="division" id="divisionId"
                             onchange="updateManagerSelect(this.value);"
                             onmouseover="tooltip.show(getTitle(this));"
                             onmouseout="tooltip.hide();">
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
                             onchange="updateManagerSelect(this.value);"
                             onmouseover="tooltip.show(getTitle(this));"
                             onmouseout="tooltip.hide();">
                    <form:options items="${divisionsList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="lowspace ">Руководитель:</span>
            </td>
            <td>
                <form:select path="manager" id="managerId"
                             onmouseover="tooltip.show(getTitle(this));"
                             onmouseout="tooltip.hide();">
                </form:select>
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
                                data-dojo-type="DateTextBox" required="true"
                                onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"/>
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
                                data-dojo-type="DateTextBox" required="true"
                                onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"/>
                </div>
            </td>
        </tr>
        <tr>
            <td>
                <span class="lowspace">Тип проекта:</span>
            </td>
            <td>
                <form:select path="state" id="divisionId"
                             onmouseover="tooltip.show(getTitle(this));"
                             onmouseout="tooltip.hide();">
                    <form:options items="${projectStateTypes}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="lowspace">Тип финансирования:</span>
            </td>
            <td>
                <form:select path="fundingType" id="divisionId"
                             onmouseover="tooltip.show(getTitle(this));"
                             onmouseout="tooltip.hide();">
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
                        <th width="200">Наименование задачи</th>
                        <th width="200">Описание</th>
                        <th width="100">Признак активности</th>
                        <th width="100">Приоритет</th>
                    </tr>
                    <c:forEach items="${projectform.projectTasks}" varStatus="row">
                        <tr id="projectTask_${row.index}" class="task_row">
                            <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
                                <td>
                                    <img class="iconbutton" title="Удалить" id="taskDeleteButton_${row.index}"
                                         src="<c:url value="/resources/img/delete.png"/>"
                                         onclick="deleteTask(${row.index});"/>
                                </td>
                            </sec:authorize>
                            <td class="multiline">
                                <form:textarea path="projectTasks[${row.index}].name" cssClass="multiline"/>
                            </td>
                            <td class="multiline">
                                <form:textarea path="projectTasks[${row.index}].description" cssClass="multiline"/>
                            </td>
                            <td>
                                <form:checkbox path="projectTasks[${row.index}].active"/>
                            </td>
                            <td>
                                <form:textarea path="projectTasks[${row.index}].priority" cssClass="multiline"/>
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
                        <th width="150">Сотрудник</th>
                        <th width="150">Роль</th>
                        <th width="100">Главный</th>
                        <th width="100">Признак активности</th>
                        <th width="100">Получение рассылки</th>
                    </tr>
                    <c:forEach items="${projectform.projectManagers}" varStatus="row">
                        <tr id="projectManager_${row.index}" class="manager_row">
                            <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
                                <td>
                                    <img class="iconbutton" title="Удалить" id="managerDeleteButton_${row.index}"
                                         src="<c:url value="/resources/img/delete.png"/>"
                                         onclick="deleteManager(${row.index});"/>
                                </td>
                            </sec:authorize>
                            <td>
                                <form:select path="projectManagers[${row.index}].employee">
                                    <form:options items="${employeesList}" itemLabel="name" itemValue="id"/>
                                </form:select>
                            </td>
                            <td>
                                <form:select path="projectManagers[${row.index}].projectRole">
                                    <form:options items="${projectRolesList}" itemLabel="value" itemValue="id"/>
                                </form:select>
                            </td>
                            <td>
                                <form:checkbox path="projectManagers[${row.index}].master"/>
                            </td>
                            <td>
                                <form:checkbox path="projectManagers[${row.index}].active"/>
                            </td>
                            <td>
                                <form:checkbox path="projectManagers[${row.index}].receivingNotifications"/>
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
                        <th width="100">Дата с</th>
                        <th width="100">Дата по</th>
                        <th width="100">Основание</th>
                    </tr>
                    <c:forEach items="${projectform.projectBillables}" varStatus="row">
                        <tr id="projectBillable_${row.index}" class="billables_row">
                            <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
                                <td>
                                    <img class="iconbutton" title="Удалить" id="billableDeleteButton_${row.index}"
                                         src="<c:url value="/resources/img/delete.png"/>"
                                         onclick="deleteBillable(${row.index});"/>
                                </td>
                            </sec:authorize>
                            <td>
                                <form:select path="projectBillables[${row.index}].employee">
                                    <form:options items="${employeesList}" itemLabel="name" itemValue="id"/>
                                </form:select>
                            </td>
                            <td>
                                <form:checkbox path="projectBillables[${row.index}].billable"/>
                            </td>
                            <td>
                                <form:input path="projectBillables[${row.index}].startDate"
                                            data-dojo-type="DateTextBox"/>
                            </td>
                            <td>
                                <form:input path="projectBillables[${row.index}].endDate"
                                            data-dojo-type="DateTextBox"/>
                                </td>
                            <td>
                                <form:input path="projectBillables[${row.index}].comment"/>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <button id="saveButton" style="width: 200px" onclick="saveProject()" type="button">
                    Сохранить
                </button>
            </td>
            <td>
                <button id="cancelButton" style="width: 200px" onclick="saveProject()" type="button">
                    Отмена
                </button>
            </td>
        </tr>
    </table>
</form:form>
</body>
</html>
