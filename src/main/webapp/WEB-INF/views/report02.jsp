<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<html>
	<head>
		<title><fmt:message key="title.report02"/></title>
	</head>

	<body>

        <script type="text/javascript" src="<%= getResRealPath("/resources/js/report.js", application) %>"></script>
        <script type="text/javascript">
            dojo.ready(function () {
                dojo.require("dijit.form.DateTextBox");
                setDefaultValuesForReport2And3();
            });

            var projectList = ${projectListJson};
            var fullProjectList = ${fullProjectListJson};
            var projectListWithOwnerDivision = ${projectListWithOwnerDivisionJson};
            var employeeList = ${employeeListJson};
        </script>

		<h1><fmt:message key="title.reportparams"/></h1>
		<h2><fmt:message key="title.report02"/></h2>
		<br/>

		<c:url value="/managertools/report/2" var="formUrl" />
		<form:form commandName="reportForm" method="post" action="${formUrl}">
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
						<td  style="width: 225px"><span class="label"><fmt:message key="label.r02.project.owner.center"/></span></td>
						<td><form:select id="divisionId" name="divisionOwnerId" cssClass="without_dojo"
									 onmouseover="tooltip.show(getTitle(this));"
									 onmouseout="tooltip.hide();" path="divisionOwnerId"
									 onchange="fillProjectListByDivision(this)"
									 oninit="">
								<form:option label="Все" value="0"/>
								<form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
							</form:select></td>
                        <fmt:message key='label.r02.show.inactive.project' var="showInactiveLabel"/>
                        <td colspan="2" align="right"><form:checkbox path="showInactiveProjects" name="showInactiveProjects"
                                                                     id="showInactiveProjects"
                                                                     cssClass="checkbox_without_dojo"
                                                                     onchange="fillProjectListByDivision()"
                                                                     label="${showInactiveLabel}"/></td>
					</tr>
					<tr>
						<td><span class="label"><fmt:message key="label.r02.project"/></span></td>
						<td><form:select id="projectId" name="projectId" cssClass="without_dojo"
									 onmouseover="tooltip.show(getTitle(this));"
									 onmouseout="tooltip.hide();" path="projectId">
							</form:select>
						</td>
                        <fmt:message key='label.r02.show.inactive.employee' var="showInactiveEmployeeLabel"/>
                        <td colspan="2" align="right"><form:checkbox path="showInactiveEmployees" name="showInactiveEmployees"
                                                                     id="showInactiveEmployees"
                                                                     cssClass="checkbox_without_dojo"
                                                                     onchange="fillEmployeeListByDivision(dojo.byId('emplDivisionId'))"
                                                                     label="${showInactiveEmployeeLabel}"/></td>
                    </tr>
					<tr>
						<td><span class="label"><fmt:message key="label.r02.employee.center"/></span></td>
						<td><form:select id="emplDivisionId" name="emplDivisionId" cssClass="without_dojo"
									 onmouseover="tooltip.show(getTitle(this));"
									 onmouseout="tooltip.hide();" path="emplDivisionId"
									 onchange="fillEmployeeListByDivision(this)">
								<form:option label="Все" value="0"/>
								<form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
							</form:select></td>
						<td><span class="label"><fmt:message key="label.r02.employee"/></span></td>
						<td><form:select path="employeeId" id="employeeId" class="without_dojo"
									 onmouseover="tooltip.show(getTitle(this));"
									 onmouseout="tooltip.hide();">
								<form:option label="Все" value="0"/>
							</form:select></td>
					</tr>
					<tr>
						<td><span class="label"><fmt:message key="label.r02.begin.date"/></span><span style="color:red">*</span></td>
						<td><form:input path="beginDate" id="beginDate" name="beginDate" class="date_picker"
									data-dojo-id="fromDate"
									data-dojo-type='dijit/form/DateTextBox'
									required="false"
									onmouseover="tooltip.show(getTitle(this));"
									onmouseout="tooltip.hide();"/></td>
						<td><span class="label"><fmt:message key="label.r02.end.date"/></span><span style="color:red">*</span></td>
						<td><form:input path="endDate" id="endDate" name="endDate" class="date_picker"
									data-dojo-id="toDate"
									data-dojo-type='dijit/form/DateTextBox'
									required="false"
									onmouseover="tooltip.show(getTitle(this));"
									onmouseout="tooltip.hide();"/></td>
					</tr>
                    <tr>
                        <td style="width: 225px">
                            <span class="label" style="float:left"><fmt:message key="label.r02.region"/></span><span style="color:red">*</span>
							<span style="float: right">
								<span>
									<form:checkbox  id="allRegions" name="allRegions"  path="allRegions"
                                                    onchange="allRegionsCheckBoxChange(this.checked)" />
								</span>
								<span><fmt:message key="label.r02.all.regions"/></span>
							</span>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:select id="regionIds" name="regionIds"
                                         onmouseover="tooltip.show(getTitle(this));"
                                         onmouseout="tooltip.hide();" path="regionIds" multiple="true"
                                         cssClass ="region">
                                <form:options items="${regionList}" itemLabel="name" itemValue="id"/>
                            </form:select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="4">
                            <form:checkbox  id="showNonBillable" name="showNonBillable"  path="showNonBillable"/>
                            <label for="showNonBillable"><fmt:message key="label.r01.show.non.billable"/></label>
                        </td>
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
                        <li><input type=radio name="printtype" id="printtype3" value="3" disabled/>
                            <label for="printtype3"><fmt:message key="label.report.pdf"/></label>
                        </li>
                    </ul>
                </div>

			</div>

			<button id="make_report_button" style="width:210px" type="submit" onclick="clearErrorBox('errorBoxId')"><fmt:message key="label.report.form"/></button>
		</form:form>
	</body>

</html>
