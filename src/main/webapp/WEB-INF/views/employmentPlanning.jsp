<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%--ToDo тоже удалить--%>
<%@ page import="static com.aplana.timesheet.form.AddEmployeeForm.*" %>
<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@ page import="static com.aplana.timesheet.system.constants.TimeSheetConstants.DOJO_PATH" %>

<html>
<head>
    <title></title>
    <script src="<%= getResRealPath("/resources/js/employmentPlanning.js", application) %>" type="text/javascript"></script>
    <style type="text/css">
        @import "<%= DOJO_PATH %>/dojox/grid/resources/tundraGrid.css";
        @import "<%= getResRealPath("/resources/css/employmentPlanning.css", application) %>";

        <%--Для сокрытия строк грида, выходящих за грид--%>
        div.dojoxGridRow {
            width: 100%;
        }

        .tundra .dojoxGridHeader {
            background-color: white;
        }

        .editedCell {
            color: #FF8500;
            text-align: center;
        }

        .employeeLabel {
            font-size: 14px;
        }
    </style>

    <script type="text/javascript">
        // Значения, для которых отображаются гриды
        var gMonthBegin = ${form.monthBeg};
        var gYearBegin = ${form.yearBeg};
        var gMonthEnd = ${form.monthEnd};
        var gYearEnd = ${form.yearEnd};
        var gProjectId = ${form.projectId};

        var deleteImg = "<c:url value="/resources/img/delete.png"/>";
        var okImg = '<c:url value="/resources/img/ok.png"/>';
        var addImg = '<c:url value="/resources/img/add.gif"/>';
        var managerMapJson = ${managerList};
    </script>
</head>

<body>
<form:form method="post" commandName="employmentPlanningForm" cssClass="employmentPlanningForm">
    <table class="no_border employmentPlanningTable">
        <tr>
            <td><span class="label">Подразделение</span></td>
            <td colspan="2">
                <form:select path="selectDivisionId" onchange="updateProjectList()" cssClass="bigSelect">
                    <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Месяц, год начала периода:</span>
            </td>
            <td>
                <form:select path="monthBeg" onchange="updateProjectList()">
                    <form:options items="${monthList}" itemLabel="monthTxt" itemValue="month"/>
                </form:select>
            </td>
            <td>
                <form:select path="yearBeg" onchange="updateProjectList()">
                    <form:options items="${yearList}" itemLabel="year" itemValue="year"/>
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Месяц, год конца периода:</span>
            </td>
            <td>
                <form:select path="monthEnd">
                    <form:options items="${monthList}" itemLabel="monthTxt" itemValue="month"/>
                </form:select>
            </td>
            <td>
                <form:select path="yearEnd">
                    <form:options items="${yearList}" itemLabel="year" itemValue="year"/>
                </form:select>
            </td>
            <td></td>
        </tr>
        <tr>
            <td>
                <span class="label">Выбор проекта:</span>
            </td>
            <td colspan="2">
                <form:select path="projectId" cssClass="bigSelect" onchange="onProjectChanged();">
                    <form:option label="" value="${all}"/>
                    <form:options items="${projectList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
            <td></td>
        </tr>
        <tr>
            <td>
                <input style="width:150px;margin-left: 23px;" type="button" value="Сохранить"
                       onclick="saveProjectPlan();"/>
            </td>
        </tr>
    </table>
</form:form>

<div class="errors_box" id="errorBox" style="display: none"></div>
<div id="grids" style="display: none">
    <div id="projectGridDiv" style="width: auto; max-width: 100%;"></div>
    <br/>

    <span id="spanEmployeeName" class="employeeLabel">&nbsp;</span>

    <div id="divEmployeeInfo" hidden="true">
        <input type="checkbox" id="isFactCheckBox" onclick="hideFact()" checked><span class="employeeLabel">Отображать фактическое значение</span></input>
        <br/>

        <div id="employeeGridDiv"></div>
    </div>
</div>

<%--ToDo брать этот кусок из общих jsp--%>
<div data-dojo-type="dijit/Dialog" data-dojo-id="employeeDialog" title="Добавить сотрудника" id="employeeDialog" style="display: none">

    <form:form commandName="<%= ADD_FORM %>">
        <table class="dijitDialogPaneContentArea no_border employmentPlanningTable">
            <tr>
                <td><label>Подразделение</label></td>
                <td>
                    <form:select path="divisionId" onchange="updateManagerListByDivision(); updateAdditionEmployeeList()">
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
</body>
</html>