<%--
ToDo форма сейчас не является универсальной.
Необходимо будет её доработать и сделать таковой, для этого необходимо просмотреть работу формы employmentPlanning.jsp
Если будет необходимо создать ещё одну такую форму, то необходимо обязательно, объединить функционал на этих двух формах
и использовать в новом месте.
Для дальнейших консультаций обращаться к iziyangirov@aplana.com (zildarius@mail.ru)
--%>
<%--
Форма для добавления сотрудников в различные таблицы
Необходимо в своем методе переопределить функцию returnEmployees,
которая бы получала данные с формы (additionEmployeeList) и использовала их.
Пример получения в overtimeTable.jsp

В моделе должны быть divisionList, projectRoleList, regionList, добавляются использованием
com.aplana.timesheet.controller.AbstractControllerForEmployee.fillMavForAddEmployeesForm
Так же необходимо в js определить переменную managerMapJson, которая получает значения из managerList, см. функцию выше

Для корректной работы необходимо подключать к своей странице
скрипты из \js\commonJS\addEmployeesForm.js

--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="static com.aplana.timesheet.form.AddEmployeeForm.*" %>

<div data-dojo-type="dijit/Dialog" data-dojo-id="addEmployeesForm_employeeDialog" title="Добавить сотрудника">

    <form:form commandName="<%= ADD_FORM %>">
        <table class="dijitDialogPaneContentArea no_border employmentPlanningTable">
            <tr>
                <td><label>Центр владельца проекта</label></td>
                <td>
                    <form:select id="addEmployeesForm_divisionOwnerId" path="divisionOwnerId"
                                 onchange="addEmployeesForm_updateProjectList()">
                        <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                    </form:select>
                </td>
                <td rowspan="7"><label>Сотрудники </label>
                    <select id="addEmployeesForm_additionEmployeeList" multiple="true" style="height: 450px"/>
                <td>
            </tr>

            <tr>
                <td><label>Тип</label></td>
                <td>
                    <form:select id="addEmployeesForm_projectTypeId" path="projectTypeId"
                                 onchange="addEmployeesForm_updateProjectList()">
                        <form:options items="${projectTypeList}" itemLabel="name" itemValue="id"/>
                    </form:select>
                </td>
            </tr>
            <tr>
                <td><label>Проект</label></td>
                <td>
                    <form:select id="addEmployeesForm_projectId" path="projectId" onchange=""></form:select>
                </td>
            </tr>
            <tr>
                <td><label>Подразделение сотрудника</label></td>
                <td>
                    <form:select id="addEmployeesForm_divisionId" path="divisionId"
                                 onchange="addEmployeesForm_updateManagers(); addEmployeesForm_updateAdditionEmployeeList();">
                        <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                    </form:select>
                <td>
            </tr>
            <tr>
                <td><label>Руководитель </label></td>
                <td>
                    <form:select id="addEmployeesForm_managerId" path="managerId"
                                 onchange="addEmployeesForm_updateAdditionEmployeeList()">
                        <form:option label="Все руководители" value="${all}"/>
                    </form:select>
                <td>
            </tr>
            <tr>
                <td><label>Должности </label></td>
                <td>
                    <form:select id="addEmployeesForm_projectRoleListId" path="projectRoleListId" multiple="true"
                                 onchange="addEmployeesForm_updateAdditionEmployeeList()" style="height: 110px">
                        <form:options items="${projectRoleList}" itemLabel="name" itemValue="id"/>
                    </form:select>
                <td>
            </tr>
            <tr>
                <td><label>Регионы </label></td>
                <td>
                    <form:select id="addEmployeesForm_regionListId" path="regionListId" multiple="true"
                                 onchange="addEmployeesForm_updateAdditionEmployeeList()"  style="height: 200px">
                        <form:options items="${regionList}" itemLabel="name" itemValue="id"/>
                    </form:select>
                <td>
            </tr>
        </table>

        <div class="dijitDialogPaneActionBar">
            <button type="button" onclick="returnEmployees(); addEmployeesForm_employeeDialog.hide();">Добавить</button>
            <button type="button" onclick="addEmployeesForm_employeeDialog.hide();">Отмена</button>
        </div>

    </form:form>
</div>


<script type="text/javascript">

    dojo.addOnLoad(function () {
        addEmployeesForm_updateLists();
        var ownerDiv = getCookieValue('aplanaDivision');
        ownerDiv = ownerDiv == undefined ? 0 : ownerDiv;
        dojo.byId("addEmployeesForm_divisionOwnerId").value = ownerDiv;
        dojo.byId("addEmployeesForm_divisionId").value = ownerDiv;
    });

    function addEmployeesForm_updateLists(){
        addEmployeesForm_updateProjectList();
        addEmployeesForm_updateManagers();
        addEmployeesForm_updateAdditionEmployeeList();
        // Дизейблим подразделения
        dojo.byId("addEmployeesForm_divisionOwnerId").disabled = true;
        dojo.byId("addEmployeesForm_divisionId").disabled = true;
    }

    function addEmployeesForm_updateProjectList(){
        fillProjectListByDivision(
                dojo.byId("addEmployeesForm_divisionOwnerId").value,
                dojo.byId("addEmployeesForm_projectId"),
                dojo.byId("addEmployeesForm_projectTypeId").value);
        dojo.byId("addEmployeesForm_projectId").remove(0);
    }

    function addEmployeesForm_updateManagers(){
        updateManagerListByDivision(
                0, managerMapJson, dojo.byId("addEmployeesForm_divisionId"), dojo.byId("addEmployeesForm_managerId"));
    }

    // Обновляет список сотрудников на форме добавления сотрудников
    function addEmployeesForm_updateAdditionEmployeeList() {
        var divisionId = dojo.byId("addEmployeesForm_divisionId").value;
        var managerId = dojo.byId("addEmployeesForm_managerId").value;
        var projectRoleListId = getSelectValues(dojo.byId("addEmployeesForm_projectRoleListId"));
        var regionListId = getSelectValues(dojo.byId("addEmployeesForm_regionListId"));

        // Делает ajax запрос, возвращающий сотрудников по центру/руководителю/должности/региону,
        processing();
        dojo.xhrGet({
            url: "/employmentPlanning/getAddEmployeeListAsJSON",
            content: {
                divisionId: divisionId,
                managerId: managerId,
                projectRoleListId: projectRoleListId,
                regionListId: regionListId
            },
            handleAs: "text",
            load: function (response, ioArgs) {
                updateEmployeeList(response);
                stopProcessing();
            },
            error: function (response, ioArgs) {
                stopProcessing();
                alert('При запросе списка сотрудников произошла ошибка.');
            }
        });

        function updateEmployeeList(response) {
            var employeeSelect = dojo.byId("addEmployeesForm_additionEmployeeList");
            employeeSelect.options.length = 0;
            var selectSize = 0;
            dojo.forEach(dojo.fromJson(response), function (row) {
                ++selectSize;
                dojo.create("option",
                        { value: row["employee_id"],
                          innerHTML: row["employee_name"],
                          div_id:   row["division"].divisionId,
                          div_name: row["division"].divisionName,
                          reg_id:   row["region"].regionId,
                          reg_name: row["region"].regionName,
                        },
                        employeeSelect);
            });
            if (selectSize == 0) {
                dojo.create("option", { value: "-1", innerHTML: "Сотрудников не найдено", disabled: true}, employeeSelect);
            }
        };
    }

</script>
