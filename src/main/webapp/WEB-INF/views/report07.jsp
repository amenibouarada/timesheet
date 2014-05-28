<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<html>
    <head>
        <title><fmt:message key="title.report07"/></title>
    </head>

    <body>
        <script type="text/javascript" src="<%= getResRealPath("/resources/js/report.js", application) %>"></script>
        <script type="text/javascript">
            dojo.ready(function () {
                var defaultDivision = "${employeeDivision}";
                var lastDivision = "${reportForm.divisionOwner}";
                if (lastDivision != "") {
                    defaultDivision = lastDivision;
                }
                dojo.byId("divisionOwner").value = defaultDivision;
                dojo.require("dijit.form.DateTextBox");

            })
        </script>

        <h1><fmt:message key="title.reportparams"/></h1>
        <h2><fmt:message key="title.report07"/></h2>
        <br/>

        <form:form commandName="reportForm" method="post" action="">

            <c:if test="${fn:length(errors) > 0}">
                <div id="errorBoxId" class="errors_box">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error.code}">
                            <fmt:param value="${error.arguments[0]}"/>
                        </fmt:message><br/>
                    </c:forEach>
                </div>
            </c:if>

            <div id="form_header">
                <table class="report_params" cellspacing="3">
                    <tr>
                        <td  style="width: 225px"><span class="label">Центр владельца проекта</span></td>
                        <td>
                            <form:select path="divisionOwner" class="without_dojo">
                                <form:option label="Все" value="0"/>
                                <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                            </form:select>
                        </td>
                    </tr>
                    <tr>
                        <td><span class="label">Центр сотрудников </span></td>
                        <td colspan="2">
                            <form:select path="divisionEmployee" class="without_dojo">
                                <form:option value="0">Все</form:option>
                                <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                            </form:select>
                        </td>
                    </tr>
                    <tr>
                        <td><span class="label">Отчетный период </span></td>
                        <td colspan="2">
                            <form:select path="periodType" class="without_dojo">
                                <form:option value="1">Месяц</form:option>
                                <form:option value="3">Квартал</form:option>
                                <form:option value="6">Полгода</form:option>
                                <form:option value="12">Год</form:option>
                            </form:select>
                        </td>
                    </tr>
                    <tr>
                        <td><span class="label">Начало периода</span><span style="color:red">*</span> </td>
                        <td><form:input path="beginDate" id="beginDate" name="beginDate" class="date_picker"
                                        data-dojo-id="fromDate"
                                        data-dojo-type='dijit/form/DateTextBox'
                                        required="false"/></td>
                        <td><span class="label">Окончание периода</span><span style="color:red">*</span> </td>
                        <td><form:input path="endDate" id="endDate" name="endDate" class="date_picker"
                                        data-dojo-id="toDate"
                                        data-dojo-type='dijit/form/DateTextBox'
                                        required="false"/></td>
                    </tr>
                </table>
                <div class="radiogroup">
                    <div class="label"><fmt:message key="report.formattitle"/></div>
                    <ul class="radio">
                        <li><input type=radio name="printtype" id="printtype2" value="2" checked/>
                            <label for="printtype2"><fmt:message key="label.report.excel"/></label>
                        </li>
                        <li><input type=radio name="printtype" id="printtype1" value="1" disabled/>
                            <label for="printtype1"><fmt:message key="label.report.html"/></label>
                        </li>
                    </ul>
                </div>
            </div>
            <button id="make_report_button" style="width:210px" type="submit" onclick="clearErrorBox('errorBoxId')">Сформировать отчет</button>
        </form:form>
    </body>

</html>
