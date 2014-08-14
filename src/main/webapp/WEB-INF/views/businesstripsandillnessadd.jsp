<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<html>
<head>
    <title>
        <c:choose>
                <c:when test="${reportId == null}">
                    <fmt:message key="title.businesstripsandillnessadd"/>
                </c:when>
            <c:when test="${reportId != null}">
                <fmt:message key="title.businesstripsandillnessedit"/>
            </c:when>
        </c:choose>
    </title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/resources/css/businesstripsandillnessadd.css">
    <script type="text/javascript">
        var employeeList = ${employeeList};
        var hasReportId = ${reportId == null};
        var reportId = "${reportId}";
        var hasProjectId = ${businesstripsandillnessadd.projectId != null};
        var contextPath = "<%= request.getContextPath()%>";
        var projectId = ${(businesstripsandillnessadd.projectId != null) ? businesstripsandillnessadd.projectId : 0};
        var loadingImageUrl = "<img src=\"<c:url value="/resources/img/loading_small.gif"/>\"/>";
        var employeeIdJsp = "${employeeId}";
    </script>
    <script type="text/javascript" src="<%= getResRealPath("/resources/js/businesstripandillnessadd.js", application) %>"></script>
</head>
<body>
<h1><div id="headerName"></div></h1>
    <br/>
    <form:form method="post" id="mainForm" commandName="businesstripsandillnessadd" name="mainForm" cssClass="chooseform">

        <div class="lowspace checkboxeslabel">
            Сотрудник:
        </div>

        <div class="lowspace checkboxesselect">
            <c:choose>
                <c:when test="${reportId == null}">
                    <div id="employeeId" name="employeeId"></div>
                </c:when>
                <c:otherwise>
                    ${businesstripsandillnessadd.employee.name}
                </c:otherwise>
            </c:choose>
        </div>

        <c:choose>
            <c:when test="${reportId == null}">
                <div class="checkboxeslabel lowspace">Создать:</div>
                <div class="checkboxesselect lowspace">
                    <form:select path="reportType" id="reportType" onchange="updateView(this)"
                                 onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();" required="true">
                        <form:options items="${businesstripsandillnessadd.reportTypes}" itemLabel="name" itemValue="id" required="true" cssClass="date_picker"/>
                    </form:select>
                </div>
            </c:when>
            <c:when test="${reportId != null}">
                <form:hidden path="reportType" />
            </c:when>
        </c:choose>

        <div class="checkboxeslabel lowspace">Дата с:</div>
        <div class="checkboxesselect lowspace">
            <form:input path="beginDate" id="beginDate" class="date_picker" cssClass="fullwidth date_picker" data-dojo-type="DateTextBox" required="true"
                        onMouseOver="tooltip.show(getTitle(this));"
                        onMouseOut="tooltip.hide();"
                        onchange="updateProject(); updateDateConstraints();"/>
        </div>

        <div class="checkboxeslabel lowspace">Дата по:</div>
        <div class="checkboxesselect lowspace">
            <form:input path="endDate" id="endDate" class="date_picker" cssClass="fullwidth date_picker" data-dojo-type="DateTextBox" required="true"
                        onMouseOver="tooltip.show(getTitle(this));"
                        onMouseOut="tooltip.hide();"
                        onchange="updateProject(); updateDateConstraints();"/>
        </div>

        <div id="illness" class="creationform">

            <div class="checkboxeslabel lowspace">Основание:</div>
            <div class="checkboxesselect lowspace">
                <form:select path="reason" id="reason" onMouseOver="tooltip.show(getTitle(this));"
                             onMouseOut="tooltip.hide();" multiple="false" cssClass="date_picker">
                    <form:options items="${businesstripsandillnessadd.illnessTypes}" itemLabel="name" itemValue="id" required="true"/>
                </form:select>
            </div>

        </div>

        <div id="businesstrip" class="creationform">

            <div class="checkboxeslabel lowspace">Тип:</div>
            <div class="checkboxesselect lowspace">
                <form:select path="businessTripType" id="businessTripType" onMouseOver="tooltip.show(getTitle(this));"
                             onMouseOut="tooltip.hide();" multiple="false" required="true" onchange="updateProject()">
                    <form:options items="${businesstripsandillnessadd.businessTripTypes}" itemLabel="name" itemValue="id" required="true"/>
                </form:select>
            </div>

            <div id="businesstripproject">
                <div class="checkboxeslabel lowspace">Проект:</div>
                <div class="checkboxesselect lowspace">
                    <form:select path="projectId" id="projectId" onMouseOver="tooltip.show(getTitle(this));"
                                 onMouseOut="tooltip.hide();" multiple="false" />
                </div>
            </div>

        </div>

        <div class="checkboxeslabel lowspace">Комментарий:</div>
        <div class="comment lowspace">
            <form:textarea path="comment" id="comment" maxlength="600" rows="7" cssClass="fullwidth"/>
        </div>


        <div style="clear:both"/>

        <div class="bigspace onblock">
            <button id="create" type="button" class="button bigspace" onclick="submitform();">Сохранить</button>
            <button id="cancel" type="button" class="button bigspace" onclick="window.history.back()">Отмена</button>
        </div>

        <div id="errorboxdiv" name="errorboxdiv" class="off errorbox">
        </div>
        <div id="servervalidationerrorboxdiv" name="servervalidationerrorboxdiv" class="errorbox">
            <form:errors path="*" delimiter="<br/><br/>" />
        </div>

    </form:form>

</body>
</html>