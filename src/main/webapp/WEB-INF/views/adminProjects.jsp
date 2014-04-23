<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="adminprojects"/></title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/resources/css/adminProjects.css">
    <script type="text/javascript">

        var mainProjectManagersJSON = ${mainProjectManagersJSON};
        var formDivisionId = "${divisionId}";
        var formManagerId = "${managerId}";
        var formShowActiveOnly = ("${showActiveOnly}" == "true");

        dojo.ready(function () {
            window.focus();
            dojo.byId("divisionId").value = formDivisionId;
            addAllAndNullDivisionSelectOptions();
            updateManagerSelect(dojo.byId("divisionId").value);
            dojo.byId("managerId").value = formManagerId;
            dojo.byId("showActiveOnly").checked = formShowActiveOnly;
        });

        /**
        * Добавляет в поле выбора подразделения варианты "Все" и "Null"
        */
        function addAllAndNullDivisionSelectOptions() {
            var divisionSelect = dojo.byId("divisionId");

            var nullDivisionOption = dojo.doc.createElement("option");
            dojo.attr(nullDivisionOption, {
                value: 0
            });
            nullDivisionOption.title = "Не проставлено (Null)";
            nullDivisionOption.innerHTML = "Не проставлено (Null)";
            divisionSelect.insertBefore(nullDivisionOption, divisionSelect.firstChild);

            var allDivisionsOption = dojo.doc.createElement("option");
            dojo.attr(allDivisionsOption, {
                value: -1
            });
            allDivisionsOption.title = "Все подразделения";
            allDivisionsOption.innerHTML = "Все подразделения";
            divisionSelect.insertBefore(allDivisionsOption, divisionSelect.firstChild);
        }

        /**
        * Заполняет список доступных для выбора руководителей проектов.
        * @param divisionId Идентификатор выбранного центра
        */
        function updateManagerSelect(divisionId) {
            var managerSelect = dojo.byId("managerId");
            var previousManager = managerSelect.value;

            var allManagersOption = dojo.doc.createElement("option");
            dojo.attr(allManagersOption, {
                value: -1
            });
            allManagersOption.title = "Все руководители";
            allManagersOption.innerHTML = "Все руководители";

            managerSelect.options.length = 0;
            managerSelect.appendChild(allManagersOption);

            if (mainProjectManagersJSON.length > 0) {
                var managerMap = mainProjectManagersJSON;
                dojo.forEach(dojo.filter(managerMap, function (division) {
                    return (division.divisionId == divisionId);
                }), function (divisionData) {
                    dojo.forEach(divisionData.managers, function(managerData) {
                        var option = document.createElement("option");
                        dojo.attr(option, {
                            value:managerData.managerId
                        });
                        option.title = managerData.name;
                        option.innerHTML = managerData.name;
                        if (managerData.managerId == previousManager) {
                            option.selected = "selected";
                        }
                        managerSelect.appendChild(option);
                    });
                });
            }
            if (managerSelect.options.length == 1){
                dojo.byId("managerId").disabled = 'disabled';
            } else {
                dojo.byId("managerId").disabled = '';
            }
        }

        function createProject() {
            var divisionId = dojo.byId("divisionId").value;
            var managerId = dojo.byId("managerId").value;

            window.location = "<%=request.getContextPath()%>/admin/projects/add?divisionId=" + divisionId +
                    "&managerId=" + managerId;
        }

        function editProject(projectId) {
            window.location = "<%=request.getContextPath()%>/admin/projects/edit?projectId=" + projectId;
        }

    </script>
</head>
<body>
<h1><fmt:message key="adminprojects"/></h1>
<br/>
<form:form method="post" commandName="adminprojects" name="mainForm">
    <table class="no_border" style="margin-bottom: 20px;">
        <tr>
            <td>
                <span class="lowspace">Подразделение:</span>
            </td>
            <td>
                <form:select path="divisionId" id="divisionId" class="without_dojo"
                             onchange="updateManagerSelect(this.value);"
                             onmouseover="tooltip.show(getTitle(this));"
                             onmouseout="tooltip.hide();">
                    <form:options items="${divisionsList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="lowspace ">Руководитель:</span>
            </td>
            <td>
                <form:select path="managerId" id="managerId" class="without_dojo"
                             onmouseover="tooltip.show(getTitle(this));"
                             onmouseout="tooltip.hide();">
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="lowspace">Только активные:</span>
            </td>
            <td>
                <form:checkbox path="showActiveOnly" id="showActiveOnly"/>
            </td>
            <td colspan="2">
                <div class="floatleft lowspace">
                    <button id="show" class="butt block " onclick="">
                        Показать
                    </button>
                </div>
            </td>
        </tr>
    </table>

    <table id="projectsTable">
        <thead>
        <tr>
            <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
                <th>
                    <img class="iconbutton" title="Создать"
                         src="<c:url value="/resources/img/add.gif"/>"
                         onclick="createProject();"/>
                </th>
            </sec:authorize>
            <th width="200">Центр компетенции</th>
            <th width="200">Руководитель проекта</th>
            <th width="200">Краткое наименование</th>
            <th width="100">Тип</th>
            <th width="100">Признак активности</th>
            <th width="100">Дата начала</th>
            <th width="100">Дата окончания</th>
            <th width="100">Заказчик</th>
        </tr>
        </thead>
        <c:choose>
            <c:when test="${fn:length(projects) > 0}">
                <tbody>
                <c:forEach var="project" items="${projects}">
                    <tr>
                        <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
                            <td width="50">
                                <img class="iconbutton" title="Изменить"
                                     src="<c:url value="/resources/img/edit.png"/>"
                                     onclick="editProject(${project.id});"/>
                                <img class="iconbutton" title="Удалить"
                                     src="<c:url value="/resources/img/delete.png"/>"
                                     onclick="deleteProject(${project.id});"/>
                            </td>
                        </sec:authorize>
                        <td class="textcenter">${project.division.name}</td>
                        <td class="textcenter">${project.manager.name}</td>
                        <td class="textcenter">${project.name}</td>
                        <td class="textcenter">${project.state.value}</td>
                        <td class="textcenter"><input type="checkbox" <c:if test="${project.active}">checked="checked"</c:if> disabled = "true"/></td>
                        <td class="textcenter"><fmt:formatDate value="${project.startDate}" pattern="dd.MM.yyyy"/></td>
                        <td class="textcenter"><fmt:formatDate value="${project.endDate}" pattern="dd.MM.yyyy"/></td>
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
