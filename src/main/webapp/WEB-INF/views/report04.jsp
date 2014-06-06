<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<html>
<head>
    <title><fmt:message key="title.report04"/></title>
</head>

<body>

<script type="text/javascript" src="<%= getResRealPath("/resources/js/report.js", application) %>"></script>
<script type="text/javascript">
    dojo.ready(function () {
        dojo.require("dijit.form.DateTextBox");
    });
</script>
<script>
    function ping() {
        setInterval(function() {
            require(["dojo/request"], function(request){
                request("/ping").then(

                );
            });
        },60000);
    }

</script>

<h1><fmt:message key="title.reportparams"/></h1>
<h2><fmt:message key="title.report04"/></h2>
<br/>

<c:url value="/managertools/report/4" var="formUrl" />
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
                <td><span class="label">Центр</span></td>
                <td><form:select id="divisionList" name="divisionOwnerList" cssClass="without_dojo"
                                 onmouseover="tooltip.show(getTitle(this));"
                                 onmouseout="tooltip.hide();" path="divisionOwnerId">
                    <form:option label="Все центры" value="0"/>
                    <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                </form:select></td>
            </tr>
            <tr>
                <td><span class="label">Начало периода</span><span style="color:red">*</span></td>
                <td><form:input path="beginDate" id="beginDate" name="beginDate" class="date_picker"
                                data-dojo-id="fromDate"
                                data-dojo-type='dijit/form/DateTextBox'
                                required="false"
                                onmouseover="tooltip.show(getTitle(this));"
                                onmouseout="tooltip.hide();"/></td>
                <td><span class="label">Окончание периода</span><span style="color:red">*</span></td>
                <td><form:input path="endDate" id="endDate" name="endDate" class="date_picker"
                                data-dojo-id="toDate"
                                data-dojo-type='dijit/form/DateTextBox'
                                required="false"
                                onmouseover="tooltip.show(getTitle(this));"
                                onmouseout="tooltip.hide();"/></td>
            </tr>
            <tr>
                <td style="width: 225px">
                    <span class="label" style="float:left">Регион</span>
							<span style="float: right">
								<span>
									<form:checkbox  id="allRegions" name="allRegions"  path="allRegions"
                                                    onchange="allRegionsCheckBoxChange(this.checked)" />
								</span>
								<span>Все регионы</span>
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

    <button id="make_report_button" style="width:210px" type="submit" onclick="clearErrorBox('errorBoxId'); ping();">Сформировать отчет</button>
</form:form>
</body>

</html>
