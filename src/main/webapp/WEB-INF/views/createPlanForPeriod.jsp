<%@ page import="com.aplana.timesheet.controller.CreatePlanForPeriodContoller" %>
<%@ page import="com.aplana.timesheet.controller.PlanEditController" %>
<%@ page import="com.aplana.timesheet.form.CreatePlanForPeriodForm" %>
<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<html>
<head>
    <title><fmt:message key="title.createPlanForPeriod" /></title>
    <script src="<%= getResRealPath("/resources/js/utils.js", application) %>" type="text/javascript"></script>

    <style type="text/css">
        table.form td {
            border: none;
            padding: 2px 0;
        }
    </style>

    <script type="text/javascript">
        dojo.require(DATE_TEXT_BOX_EXT_PATH);
        dojo.require(CALENDAR_EXT_PATH);

        initCurrentDateInfo("${createPlanForPeriodForm.employeeId}");

        dojo.declare("Calendar", com.aplana.dijit.ext.SimpleCalendar, {
            getEmployeeId: getEmployeeId
        });

        dojo.declare("DateBox", com.aplana.dijit.ext.DateTextBox, {
            popupClass: "Calendar"
        });

        function getEmployeeId() {
            return dojo.byId("<%= CreatePlanForPeriodForm.EMPLOYEE_ID %>").value;
        }

        function isDisabledFromDate(date) {
            var curDate = new Date();

            return (date.getMonth() < curDate.getMonth() || date.getFullYear() < curDate.getFullYear());
        }

        function isDisabledToDate(date) {
            var fromDate = dijit.byId("<%= CreatePlanForPeriodForm.FROM_DATE %>").get('value');

            return (date < fromDate);
        }

        function updateDateConstraintsAndProjectList() {
            var fromDateBox = dijit.byId("<%= CreatePlanForPeriodForm.FROM_DATE %>");
            var fromDate = fromDateBox.get('value');
            var curDate = new Date();

            fromDateBox.set('constraints', { min: new Date(Date.UTC(curDate.getFullYear(), curDate.getMonth())) });

            dijit.byId("<%= CreatePlanForPeriodForm.TO_DATE %>").set('constraints', { min: fromDate });

            var requestContent = {};
            var fromDateParam = formatDate(dijit.byId("<%= CreatePlanForPeriodForm.FROM_DATE %>").get('value'));
            var toDateParam = formatDate(dijit.byId("<%= CreatePlanForPeriodForm.TO_DATE %>").get('value'));

            putParamIfNotEmpty(requestContent, "<%= CreatePlanForPeriodContoller.FROM_DATE_PARAM %>", fromDateParam);
            putParamIfNotEmpty(requestContent, "<%= CreatePlanForPeriodContoller.TO_DATE_PARAM %>", toDateParam);

            function formatDate(date) {
                return (date == null) ? "" : date.format("<%= CreatePlanForPeriodContoller.DATE_FORMAT.toLowerCase() %>");
            }

            function putParamIfNotEmpty(requestContent, paramName, paramValue) {
                if (paramValue.length > 0) {
                    requestContent[paramName] = paramValue;
                }
            }

            dojo.xhrGet({
                url: getContextPath() + "<%= CreatePlanForPeriodContoller.GET_PROJECTS_URL %>",
                headers: {
                    "If-Modified-Since":"Sat, 1 Jan 2000 00:00:00 GMT"
                },
                handleAs: "json",
                timeout: 10000,
                content:  requestContent,

                load: function(data) {
                    if (data) {
                        var select = dojo.byId("<%= CreatePlanForPeriodForm.PROJECT_ID %>");
                        var value = select.value;

                        select.options.length = 1;

                        dojo.forEach(data, function(project) {
                            var option = document.createElement("option");
                            dojo.attr(option, {
                                value:project.<%= CreatePlanForPeriodContoller.PROJECT_ID %>
                            });
                            option.text = project.<%= CreatePlanForPeriodContoller.PROJECT_NAME %>;
                            option.innerHTML = project.<%= CreatePlanForPeriodContoller.PROJECT_NAME %>;
                            select.appendChild(option);
                        });

                        select.value = value;
                    }
                }
            });
        }

        function save() {
            if (validate()) {
                dojo.byId("<%= CreatePlanForPeriodContoller.FORM %>").submit();
            }
        }

        function cancel() {
            window.location = getContextPath() + "<%= PlanEditController.PLAN_EDIT_URL %>";
        }

        function validate() {
            var errors = [];

            if (dojo.byId("<%= CreatePlanForPeriodForm.EMPLOYEE_ID %>").value == 0) {
                errors.push("Сотрудник не выбран");
            }

            if (dojo.byId("<%= CreatePlanForPeriodForm.PROJECT_ID %>").value == 0) {
                errors.push("Проект не выбран");
            }

            if (!(
                    dijit.byId("<%= CreatePlanForPeriodForm.FROM_DATE %>").isValid() &&
                    dijit.byId("<%= CreatePlanForPeriodForm.TO_DATE %>").isValid()
            )) {
                errors.push("Неверный формат даты");
            }

            if (!dijit.byId("<%= CreatePlanForPeriodForm.PERCENT_OF_CHARGE %>").isValid()) {
                errors.push("Некорректное значение процента загрузки");
            }

            return !showErrors(errors);
        }
    </script>
</head>
<body>

<h1><fmt:message key="title.createPlanForPeriod"/></h1>
<br/>

<form:form commandName="<%= CreatePlanForPeriodContoller.FORM %>" method="post" cssStyle="margin: 10px 0;">
    <form:errors path="*" cssClass="errors_box" delimiter="<br/><br/>" />

    <table class="form">
        <tr>
            <td>
                <span class="label">Сотрудник</span>
            </td>
            <td>
                <form:select path="<%= CreatePlanForPeriodForm.EMPLOYEE_ID %>" cssClass="without_dojo"
                             onmouseover="showTooltip(this)" onmouseout="tooltip.hide()">
                    <form:option value="0" label="" />
                    <form:options items="${employeeList}" itemValue="id" itemLabel="name" />
                </form:select>
            </td>
            <td>
                <span class="label">Проект</span>
            </td>
            <td>
                <form:select path="<%= CreatePlanForPeriodForm.PROJECT_ID %>" cssClass="without_dojo"
                             onmouseover="showTooltip(this)" onmouseout="tooltip.hide()">
                    <form:option value="0" label="" />
                    <form:options items="${projectList}" itemValue="id" itemLabel="name" />
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Дата с</span>
            </td>
            <td>
                <form:input path="<%= CreatePlanForPeriodForm.FROM_DATE %>" required="true" data-dojo-type="DateBox"
                            cssClass="date_picker" isDisabledDate="isDisabledFromDate"
                            onChange="updateDateConstraintsAndProjectList()" />
            </td>
            <td>
                <span class="label">Дата по</span>
            </td>
            <td>
                <form:input path="<%= CreatePlanForPeriodForm.TO_DATE %>" required="true" data-dojo-type="DateBox"
                            cssClass="date_picker" isDisabledDate="isDisabledToDate"
                            onChange="updateDateConstraintsAndProjectList()" />
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Процент загрузки</span>
            </td>
            <td colspan="2">
                <form:input path="<%= CreatePlanForPeriodForm.PERCENT_OF_CHARGE %>"
                            data-dojo-type="dijit/form/NumberTextBox"
                            constraints="{ min: 0, max: 100, fractional: false }"
                            cssStyle="width: 30px; padding: 2px;" required="true" />
                <span>%</span>
            </td>
        </tr>
    </table>

    <br />

    <button style="width: 150px;" type="button" onclick="save()">Сохранить</button>
    <button style="width: 150px;" type="button" onclick="cancel()">Отмена</button>
</form:form>

</body>
</html>