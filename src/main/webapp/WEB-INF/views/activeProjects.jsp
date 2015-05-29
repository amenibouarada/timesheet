<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title><fmt:message key="title.activeProjects"/></title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/resources/css/adminProjects.css">
    <script>
        function viewProject(id){
            var divisionId = dojo.byId("divisionId").value;
            window.location = "<%= request.getContextPath()%>/viewProjects/"+divisionId+"/"+id;
        }

        function applyFilter(){
            var form = dojo.byId("activeProjectForm");
            form.action = "<%= request.getContextPath()%>/activeProjects";
            form.submit();
        }
    </script>
</head>
<body>
<form:form method="GET" commandName="activeProjectForm" name="activeProjectForm">
    <table class="no_border" style="margin-bottom: 20px;">
        <tr>
            <td>
                <span class="lowspace">Подразделение:</span>
            </td>
            <td>
                <form:select path="divisionId" id="divisionId" class="without_dojo"
                             onmouseover="tooltip.show(getTitle(this));"
                             onmouseout="tooltip.hide();">
                    <form:options items="${divisionsList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
            <td colspan="2">
                <div class="floatleft lowspace">
                    <button id="show" class="butt block" onclick="applyFilter()">
                        Показать
                    </button>
                </div>
            </td>
        </tr>
    </table>

    <table id="projectsTable">
        <thead>
        <tr>
            <th width="26">
            </th>
            <th width="200">Центр компетенции</th>
            <th width="200">Руководитель проекта</th>
            <th width="200">Краткое наименование</th>
            <th width="100">Тип</th>
            <th width="100">Дата начала</th>
            <th width="100">Дата окончания</th>
            <th width="100">Заказчик</th>
        </tr>
        </thead>
        <c:choose>
            <c:when test="${fn:length(projects) > 0}">
                <tbody>
                <c:forEach var="project" items="${projects}" varStatus="row">
                    <tr>
                        <td>
                            <img class="iconbutton" title="Изменить"
                                 src="<c:url value="/resources/img/view.png"/>"
                                 onclick="viewProject(${project.id});"/>
                        </td>
                        <td class="textcenter">${project.division.name}</td>
                        <td class="textcenter">${project.manager.name}</td>
                        <td id="projectName_${project.id}" class="textcenter">${project.name}</td>
                        <td class="textcenter">${project.state.value}</td>
                        <td class="textcenter"><fmt:formatDate value="${project.startDate}" pattern="dd.MM.yyyy"/></td>
                        <td class="textcenter">
                            <c:if test="${project.endDate gt infiniteDate}">
                                Не определено
                            </c:if>
                            <c:if test="${project.endDate lt infiniteDate}">
                                <fmt:formatDate value="${project.endDate}" pattern="dd.MM.yyyy"/>
                            </c:if>
                            </td>
                        <td class="textcenter">${project.customer}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </c:when>
        </c:choose>
    </table>
</form:form>
</body>
</html>