<%--
Форма для добавления сотрудников в различные таблицы
Необходимо в своем методе переопределить функцию addRow,
которая бы получала данные с формы (additionEmployeeList) и использовала их.
Пример получения в monthreport.jsp

В моделе должны быть divisionList, projectRoleList, regionList, добавляются использованием
com.aplana.timesheet.controller.AbstractControllerForEmployee.fillMavForAddEmployeesForm
Так же необходимо в js определить переменную managerMapJson, которая получает значения из managerList, см. функцию выше

Для корректной работы необходимо подключать к своей странице
скрипты из \js\commonJS\addEmployeesForm.js

--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="static com.aplana.timesheet.form.AddEmployeeForm.*" %>

<div data-dojo-type="dijit/Dialog" data-dojo-id="employeeDialog" title="Добавить сотрудника">

    <form:form commandName="<%= ADD_FORM %>">
        <table class="dijitDialogPaneContentArea no_border employmentPlanningTable">
            <tr>
                <td><label>Подразделение</label></td>
                <td>
                    <form:select path="divisionId" onchange="updateManagerListByDivision(undefined, ${managerList}); updateAdditionEmployeeList()">
                        <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                    </form:select>
                <td>
                <td rowspan="4"><label>Сотрудники </label>
                    <select id="additionEmployeeList" multiple="true" style="height: 365px"/>
                <td>
            </tr>
            <tr>
                <td><label>Руководитель </label></td>
                <td>
                    <form:select path="managerId" onchange="updateAdditionEmployeeList()">
                        <form:option label="Все руководители" value="${all}"/>
                    </form:select>
                <td>
            </tr>
            <tr>
                <td><label>Должности </label></td>
                <td>
                    <form:select path="projectRoleListId" multiple="true" onchange="updateAdditionEmployeeList()" style="height: 110px">
                        <form:options items="${projectRoleList}" itemLabel="name" itemValue="id"/>
                    </form:select>
                <td>
            </tr>
            <tr>
                <td><label>Регионы </label></td>
                <td>
                    <form:select path="regionListId" multiple="true" onchange="updateAdditionEmployeeList()"  style="height: 200px">
                        <form:options items="${regionList}" itemLabel="name" itemValue="id"/>
                    </form:select>
                <td>
            </tr>
        </table>

        <div class="dijitDialogPaneActionBar">
            <button type="button" onclick="addRow();">Добавить</button>
            <button type="button" onclick="employeeDialog.hide();">Отмена</button>
        </div>

    </form:form>
</div>
