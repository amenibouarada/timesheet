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

        dojo.require("dijit.form.DateTextBox");

        dojo.declare("DateTextBox", dijit.form.DateTextBox, {
            popupClass: "dijit.Calendar"
        });

        dojo.ready(function () {
            window.focus();
            updateManagerSelect(dojo.byId("managerDivisionId").value);
            dojo.byId("managerId").value = managerId;
        });

        function saveProject() {
            var validationResult = validateForm();

            if (validationResult.length == 0) {
                projectform.action = "save";
                projectform.submit();
            } else {
                alert("Следующие поля пустые или заполнены с ошибкой:\n" + validationResult + ".\n\nСохранение отменено.");
            }
        }

        /**
         * Заполняет список доступных для выбора руководителей проектов.
         * @param divisionId Идентификатор выбранного центра
         */
        function updateManagerSelect(divisionId) {
            var managerSelect = dojo.byId("managerId");
            var previousManager = managerSelect.value;

            managerSelect.options.length = 0;

            if (divisionsEmployeesJSON.length > 0) {
                var divisionEmployees = divisionsEmployeesJSON;
                dojo.forEach(dojo.filter(divisionEmployees, function (division) {
                    return (division.divisionId == divisionId);
                }), function (divisionData) {
                    dojo.forEach(divisionData.managers, function(managerData) {
                        var option = document.createElement("option");
                        dojo.attr(option, {
                            value:managerData.employeeId
                        });

                        option.title = managerData.name;
                        option.innerHTML = managerData.name;

                        /*if (managerData.active == "active") {
                            option.title = managerData.name;
                            option.innerHTML = managerData.name;
                        } else {
                            option.title = managerData.name + " (уволен)";
                            option.innerHTML = managerData.name + " (уволен)";
                        }*/
                        managerSelect.appendChild(option);
                    });
                });
            }
            sortSelectOptions(managerSelect);

            if (managerSelect.options.length < 2){
                dojo.byId("managerId").disabled = 'disabled';
            } else {
                dojo.byId("managerId").disabled = '';
            }
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

            /*------------------------*/
            /*    Кнопка удаления     */
            /*------------------------*/

            var deleteCell = newTask.insertCell(0);
            var img = dojo.doc.createElement("img");
            dojo.attr(img, {
                id:"taskDeleteButton_" + newTaskIndex,
                class: "iconbutton",
                src:"<%= request.getContextPath()%>/resources/img/delete.png",
                alt:"Удалить",
                title:"Удалить"
            });
            img.onclick = function () {
                deleteTask(newTaskIndex);
            };
            deleteCell.appendChild(img);

            /*---------------------------*/
            /*    Наименование задачи    */
            /*---------------------------*/

            var nameCell = newTask.insertCell(1);
            dojo.addClass(nameCell , "multiline");
            var nameInput = dojo.doc.createElement("textarea");
            dojo.addClass(nameInput , "task_name");
            dojo.attr(nameInput , {
                id:"taskNameInput_" + newTaskIndex,
                name:"projectTasks[" + newTaskIndex + "].name",
                wrap:"soft",
                rows:"3"
            });
            nameCell.appendChild(nameInput);

            var _toDeleteInput = dojo.doc.createElement("input");
            dojo.addClass(_toDeleteInput , "to_delete");
            dojo.attr(_toDeleteInput , {
                id: "projectTasks" + newTaskIndex + ".toDelete",
                name:"projectTasks[" + newTaskIndex + "].toDelete",
                type:"hidden",
                value:""
            });
            nameCell.appendChild(_toDeleteInput);

            /*----------------*/
            /*    Описание    */
            /*----------------*/

            var descriptionCell = newTask.insertCell(2);
            dojo.addClass(descriptionCell , "multiline");
            var descriptionInput = dojo.doc.createElement("textarea");
            dojo.addClass(descriptionInput , "task_description");
            dojo.attr(descriptionInput , {
                id:"taskDescriptionInput_" + newTaskIndex,
                name:"projectTasks[" + newTaskIndex + "].description",
                wrap:"soft",
                rows:"3"
            });
            descriptionCell.appendChild(descriptionInput);

            /*------------------------------------*/
            /*    Чекбокс "Признак активности"    */
            /*------------------------------------*/

            var activeCell = newTask.insertCell(3);
            var activeInput = dojo.doc.createElement("input");
            dojo.attr(activeInput , {
                id:"taskActiveInput_" + newTaskIndex,
                name:"projectTasks[" + newTaskIndex + "].active",
                type:"checkbox"
            });
            activeCell.appendChild(activeInput);

            var _activeInput = dojo.doc.createElement("input");
            dojo.attr(_activeInput , {
                name:"_projectTasks[" + newTaskIndex + "].active",
                type:"hidden",
                value:"on"
            });
            activeCell.appendChild(_activeInput);


            /*-----------------*/
            /*    Приоритет    */
            /*-----------------*/

            var priorityCell = newTask.insertCell(4);
            dojo.addClass(priorityCell , "multiline");
            var priorityInput = dojo.doc.createElement("textarea");
            dojo.attr(priorityInput, {
                id:"taskPriorityInput_" + newTaskIndex,
                name:"projectTasks[" + newTaskIndex + "].priority",
                wrap:"soft",
                rows:"3"
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

            /*------------------------*/
            /*    Кнопка удаления     */
            /*------------------------*/

            var deleteCell = newManager.insertCell(0);
            var img = dojo.doc.createElement("img");
            dojo.attr(img, {
                id:"managerDeleteButton_" + newManagerIndex,
                class: "iconbutton",
                src:"<%= request.getContextPath()%>/resources/img/delete.png",
                alt:"Удалить",
                title:"Удалить"
            });
            img.onclick = function () {
                deleteManager(newManagerIndex);
            };
            deleteCell.appendChild(img);

            /*------------------------*/
            /*    Выбор сотрудника    */
            /*------------------------*/

            var managerCell = newManager.insertCell(1);
            var managerSelect = dojo.doc.createElement("select");
            dojo.attr(managerSelect, {
                id:"projectManagers" + newManagerIndex + ".employee",
                name:"projectManagers[" + newManagerIndex + "].employee"
            });
            for (var i = 0; i < employeesListJSON.length; i++) {
                var managerOption = dojo.doc.createElement("option");
                dojo.attr(managerOption, {
                    value: employeesListJSON[i].id,
                    title: employeesListJSON[i].name
                });

                managerOption.innerHTML = employeesListJSON[i].name;
                managerSelect.appendChild(managerOption);
            }
            managerCell.appendChild(managerSelect);

            var _toDeleteInput = dojo.doc.createElement("input");
            dojo.addClass(_toDeleteInput , "to_delete");
            dojo.attr(_toDeleteInput , {
                id: "projectManagers" + newManagerIndex + ".toDelete",
                name:"projectManagers[" + newManagerIndex + "].toDelete",
                type:"hidden",
                value:""
            });
            managerCell.appendChild(_toDeleteInput);

            /*------------------------*/
            /*       Выбор роли       */
            /*------------------------*/

            var roleCell = newManager.insertCell(2);
            var roleSelect = dojo.doc.createElement("select");
            dojo.attr(roleSelect, {
                id:"projectManagers" + newManagerIndex + ".projectRole",
                name:"projectManagers[" + newManagerIndex + "].projectRole"
            });
            for (var i = 0; i < projectRoleTypesJSON.length; i++) {
                var roleOption = dojo.doc.createElement("option");
                dojo.attr(roleOption, {
                    value: projectRoleTypesJSON[i].id,
                    title: projectRoleTypesJSON[i].value
                });

                roleOption.innerHTML = projectRoleTypesJSON[i].value;
                roleSelect.appendChild(roleOption);
            }
            roleCell.appendChild(roleSelect);

            /*------------------------*/
            /*   Чекбокс "Главный"    */
            /*------------------------*/

            var masterCell = newManager.insertCell(3);
            var masterInput = dojo.doc.createElement("input");
            dojo.attr(masterInput , {
                id:"projectManagers" + newManagerIndex + ".master1",
                name:"projectManagers[" + newManagerIndex + "].master",
                type:"checkbox"
            });
            masterCell.appendChild(masterInput);

            var _masterInput = dojo.doc.createElement("input");
            dojo.attr(_masterInput , {
                name:"_projectManagers[" + newManagerIndex + "].master",
                type:"hidden",
                value:"on"
            });
            masterCell.appendChild(_masterInput);

            /*----------------------------------*/
            /*   Чекбокс "Признак активности"   */
            /*----------------------------------*/

            var activeCell = newManager.insertCell(3);
            var activeInput = dojo.doc.createElement("input");
            dojo.attr(activeInput , {
                id:"projectManagers" + newManagerIndex + ".active1",
                name:"projectManagers[" + newManagerIndex + "].active",
                type:"checkbox"
            });
            activeCell.appendChild(activeInput);

            var _activeInput = dojo.doc.createElement("input");
            dojo.attr(_activeInput , {
                name:"_projectManagers[" + newManagerIndex + "].active",
                type:"hidden",
                value:"on"
            });
            activeCell.appendChild(_activeInput);
        }

        function createBillable() {
            var projectBillables = dojo.byId("projectBillables");
            var projectBillablesRows = dojo.query(".billable_row");
            var billablesCount = projectBillablesRows.length;

            var newBillable = projectBillables.insertRow(billablesCount + 1);
            dojo.addClass(newBillable, "billable_row");

            var newBillableIndex;
            if (billablesCount == 0) {
                newBillableIndex = 0;
            }
            else {
                var lastBillable = projectBillablesRows[billablesCount - 1];
                var lastBillableId = dojo.attr(lastBillable, "id");
                var lastBillableIndex = parseInt(lastBillableId.substring(lastBillableId.lastIndexOf("_") + 1, lastBillableId.length));
                newBillableIndex = lastBillableIndex + 1;
            }
            dojo.attr(newBillable, {id : "projectBillable_" + newBillableIndex});

            /*------------------------*/
            /*    Кнопка удаления     */
            /*------------------------*/

            var deleteCell = newBillable.insertCell(0);
            var img = dojo.doc.createElement("img");
            dojo.attr(img, {
                id:"billableDeleteButton_" + newBillableIndex,
                class: "iconbutton",
                src:"<%= request.getContextPath()%>/resources/img/delete.png",
                alt:"Удалить",
                title:"Удалить"
            });
            img.onclick = function () {
                deleteBillable(newBillableIndex);
            };
            deleteCell.appendChild(img);

            /*------------------------*/
            /*    Выбор сотрудника    */
            /*------------------------*/

            var employeeCell = newBillable.insertCell(1);
            var employeeSelect = dojo.doc.createElement("select");
            dojo.attr(employeeSelect, {
                id:"projectBillables" + newBillableIndex + ".employee",
                name:"projectBillables[" + newBillableIndex + "].employee"
            });
            for (var i = 0; i < employeesListJSON.length; i++) {
                var employeeOption = dojo.doc.createElement("option");
                dojo.attr(employeeOption, {
                    value: employeesListJSON[i].id,
                    title: employeesListJSON[i].name
                });

                employeeOption.innerHTML = employeesListJSON[i].name;
                employeeSelect.appendChild(employeeOption);
            }
            employeeCell.appendChild(employeeSelect);

            var _toDeleteInput = dojo.doc.createElement("input");
            dojo.addClass(_toDeleteInput , "to_delete");
            dojo.attr(_toDeleteInput , {
                id: "projectBillables" + newBillableIndex + ".toDelete",
                name:"projectBillables[" + newBillableIndex + "].toDelete",
                type:"hidden",
                value:""
            });
            employeeCell.appendChild(_toDeleteInput);

            /*--------------------------------------*/
            /*    Чекбокс "Учитывать в затратах"    */
            /*--------------------------------------*/

            var billableCell = newBillable.insertCell(2);
            var billableInput = dojo.doc.createElement("input");
            dojo.attr(billableInput , {
                id:"projectBillables" + newBillableIndex + ".billable1",
                name:"projectBillables[" + newBillableIndex + "].billable",
                type:"checkbox"
            });
            billableCell.appendChild(billableInput);

            var _activeInput = dojo.doc.createElement("input");
            dojo.attr(_activeInput , {
                name:"_projectBillables[" + newBillableIndex + "].billable",
                type:"hidden",
                value:"on"
            });
            billableCell.appendChild(_activeInput);

            /*------------------------*/
            /*         Дата с         */
            /*------------------------*/

            var startDateCell = newBillable.insertCell(3);
            dojo.addClass(startDateCell , "billable_date");
            var startDateInput = dojo.doc.createElement("input");
            dojo.attr(startDateInput , {
                id:"projectBillables" + newBillableIndex + ".startDate",
                name:"projectBillables[" + newBillableIndex + "].startDate",
                type:"text",
                "data-dojo-type": "dijit.form.DateTextBox"
            });
            startDateCell.appendChild(startDateInput);

            /*-------------------------*/
            /*         Дата по         */
            /*-------------------------*/

            var endDateCell = newBillable.insertCell(4);
            dojo.addClass(endDateCell , "billable_date");
            var endDateInput = dojo.doc.createElement("input");
            dojo.attr(endDateInput , {
                id:"projectBillables" + newBillableIndex + ".endDate",
                name:"projectBillables[" + newBillableIndex + "].endDate",
                type:"text",
                "data-dojo-type": "dijit.form.DateTextBox"
            });
            endDateCell.appendChild(endDateInput);
            dojo.parser.parse();

            /*-------------------------*/
            /*        Основание        */
            /*-------------------------*/

            var commentCell = newBillable.insertCell(5);
            dojo.addClass(commentCell , "multiline");
            var commentInput = dojo.doc.createElement("textarea");
            dojo.attr(commentInput , {
                id:"projectBillables" + newBillableIndex + ".comment",
                name:"projectBillables[" + newBillableIndex + "].comment",
                wrap:"soft",
                rows:"3"
            });
            commentCell.appendChild(commentInput);
        }

        function deleteTask(row) {
            var task = dojo.byId("projectTasks" + row + ".toDelete");
            dojo.attr(task, {
                value: "delete"
            });
            var row = dojo.byId("projectTask_" + row);
            dojo.addClass(row , "hidden_delete");
        }

        function deleteManager(row) {
            var task = dojo.byId("projectManagers" + row + ".toDelete");
            dojo.attr(task, {
                value: "delete"
            });
            var row = dojo.byId("projectManager_" + row);
            dojo.addClass(row , "hidden_delete");
        }

        function deleteBillable(row) {
            var task = dojo.byId("projectBillables" + row + ".toDelete");
            dojo.attr(task, {
                value: "delete"
            });
            var row = dojo.byId("projectBillable_" + row);
            dojo.addClass(row , "hidden_delete");
        }

        function validateForm() {
            var valid = true;
            var errors = [];

            var name = dojo.byId("name");
            dojo.style(name, "background-color", "#ffffff");
            if (name.value.length == 0) {
                valid = false;
                //name.style({"background-color" : "#FFCFCF"});
                dojo.style(name, "background-color", "#f9f7ba");
                errors.push("наименование проекта");
            }

            /*
            var customer = dojo.byId("customer");
            dojo.style(customer, "background-color", "#ffffff");
            if (customer.value.length == 0) {
                valid = false;
                //customer.style({"background-color" : "#FFCFCF"});
                dojo.style(customer, "background-color", "#f9f7ba");
                errors.push("наименование заказчика");
            }
            */

            var startDate = dojo.byId("startDate");
            dojo.style(startDate, "background-color", "#ffffff");
            if (startDate.getAttribute("aria-invalid") == "true") {
                valid = false;
                //startDate.style({"background-color" : "#FFCFCF"});
                dojo.style(startDate, "background-color", "#f9f7ba");
                errors.push("дата начала проекта");
            }

            var endDate = dojo.byId("endDate");
            dojo.style(endDate, "background-color", "#ffffff");
            if (endDate.getAttribute("aria-invalid") == "true") {
                valid = false;
                //endDate.style({"background-color" : "#FFCFCF"});
                dojo.style(endDate, "background-color", "#f9f7ba");
                errors.push("дата окончания проекта");
            }

            /*
            var jiraKey = dojo.byId("jiraKey");
            dojo.style(jiraKey, "background-color", "#ffffff");
            if (jiraKey.value.length == 0) {
                valid = false;
                dojo.style(jiraKey, "background-color", "#f9f7ba");
                errors.push("имя в Jira");
            }
            */

            var taskRequired = dojo.byId("cqRequired1");
            if (taskRequired.checked) {
                var tasks = dojo.query(".task_row:not(.hidden_delete)");
                if (tasks.length == 0) {
                    valid = false;
                    errors.push("не указаны задачи по проекту");
                }
            }

            var taskNames = dojo.query(".task_row:not(.hidden_delete) .task_name");
            var namesValid = true;
            dojo.forEach(taskNames, function(name) {
                dojo.style(name, "background-color", "#ffffff");
                if (name.value.length == 0) {
                    dojo.style(name, "background-color", "#f9f7ba");
                    namesValid = false;
                }
            });
            if (!namesValid) {
                valid = false;
                errors.push("наименование задачи");
            }

            var taskDescriptions = dojo.query(".task_row:not(.hidden_delete) .task_description");
            var descriptionsValid = true;
            dojo.forEach(taskDescriptions, function(description) {
                dojo.style(description, "background-color", "#ffffff");
                if (description.value.length == 0) {
                    dojo.style(description, "background-color", "#f9f7ba");
                    descriptionsValid = false;
                }
            });
            if (!descriptionsValid) {
                valid = false;
                errors.push("описание задачи");
            }

            var dates = dojo.query(".billable_date:not(> tr.hidden_delete)").query(".dijitInputInner");
            var billablesValid = true;
            dojo.forEach(dates, function(date) {
                if (date.getAttribute("aria-invalid") == "true") {
                    //date.style({"background-color" : "#FFCFCF"});
                    billablesValid = false;
                }
            });
            if (!billablesValid) {
                valid = false;
                errors.push("дата учёта в затратах");
            }

            if (!valid) {
                return errors.join(",\n");
            } else {
                return "";
            }
        }
    </script>
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
                <form:select path="manager" id="managerId">
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
                                               cssClass="multiline"  rows="3"/>
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
                    <c:forEach items="${projectform.projectManagers}" varStatus="row">
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
                                <form:select path="projectManagers[${row.index}].employee">
                                    <form:options items="${employeesList}" itemLabel="name" itemValue="id"/>
                                </form:select>
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
                    <c:forEach items="${projectform.projectBillables}" varStatus="row">
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
                                <form:select path="projectBillables[${row.index}].employee">
                                    <form:options items="${employeesList}" itemLabel="name" itemValue="id"/>
                                </form:select>
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
