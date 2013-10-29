<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="title.assigmentleaders"/></title>
</head>
<body>

    <script type="text/javascript">


        dojo.ready(function () {
            setFilter();
        });

        function setFilter() {
            var division = ${currentUserDivisionId};
            var filter = dojo.byId("filter");
            filter.value = division;
        }

        function filterChange(obj) {
            var divisionId = obj.value;
            if (${editable}) {
                mainForm.action = "<%=request.getContextPath()%>/admin/update/assignmentleaders/" + divisionId + "/edit";
            } else {
                mainForm.action = "<%=request.getContextPath()%>/admin/update/assignmentleaders/" + divisionId + "/view";
            }
            mainForm.submit();
        }

        function saveResult(obj) {
            var divisionId = obj.value;
            mainForm.action = "<%=request.getContextPath()%>/admin/update/assignmentleaders/save/" + divisionId;
            mainForm.submit();
        }

        function edit(obj) {
            var divisionId = obj.value;
            mainForm.action = "<%=request.getContextPath()%>/admin/update/assignmentleaders/" + divisionId + "/edit";
            mainForm.submit();
        }

        function cancel(obj) {
            var divisionId = obj.value;
            mainForm.action = "<%=request.getContextPath()%>/admin/update/assignmentleaders/" + divisionId + "/view";
            mainForm.submit();
        }

    </script>

<h1><fmt:message key="title.assigmentleaders"/></h1>

<br/><br/>
<form:form method="post" modelAttribute="assignmentLeadersForm" name="mainForm">


    <div id="filter_element">
        <span class="label">Подразделение</span>
        <form:select path="" id="filter" class="without_dojo" onchange="filterChange(this)"
                     onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
            <form:option label="" value="0"/>
            <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
        </form:select>
    </div>

    <div id="form_table">
        <table id="assignement_table">
            <tr id="assignment_header">
                <th width="20px">№</th>
                <th width="300px">Подразделение</th>
                <th width="300px">Регион</th>
                <th width="300px">Руководитель</th>
            </tr>

            <c:if test="${fn:length(assignmentLeadersForm.tableRows) > 0}">
                <c:forEach items="${assignmentLeadersForm.tableRows}" varStatus="row">
                    <tr class="assignment_row" id="assignment_row_${row.index}" style="height: 20px">
                        <td class="text_center_align row_number"><c:out value="${row.index + 1}"/></td>
                        <!-- Подразделение -->
                        <td>
                            <form:label path="tableRows[${row.index}].division">
                                <c:out value="${assignmentLeadersForm.tableRows[row.index].division}"/>
                            </form:label>
                            <form:hidden id="division_id_${row.index}" path="tableRows[${row.index}].divisionId"/>
                        </td>
                        <!-- Регион -->
                        <td>
                            <form:label path="tableRows[${row.index}].region">
                                <c:out value="${assignmentLeadersForm.tableRows[row.index].region}"/>
                            </form:label>
                            <form:hidden id="region_id_${row.index}" path="tableRows[${row.index}].regionId"/>
                        </td>
                        <!-- Руководитель -->
                        <c:if test="${editable == true}">
                            <td>
                                <form:select cssStyle="border: none; width: 300px" path="tableRows[${row.index}].leaderId" id="leader_id_${row.index}"
                                             onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                                    <form:option label="" value="0"/>
                                    <form:options items="${assignmentLeadersForm.tableRows[row.index].regionDivisionEmployees}"
                                                  itemLabel="name" itemValue="id"/>
                                </form:select>
                            </td>
                        </c:if>
                        <c:if test="${editable == false}">
                            <td>
                                <form:label path="tableRows[${row.index}].leaderId" cssStyle="border: none">
                                    <c:out value="${assignmentLeadersForm.tableRows[row.index].leader}"/>
                                </form:label>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
            </c:if>
        </table>
    </div>
    <div>
        <c:if test="${editable == true}">
            <button id="submit_button" style="width:210px" onclick="saveResult(filter)" type="button">
                Сохранить
            </button>
            <button id="cancel_button" style="width:210px" onclick="cancel(filter)" type="button">
                Отмена
            </button>
        </c:if>
        <c:if test="${editable == false}">
            <button id="edit_button" style="width:210px" onclick="edit(filter)" type="button">
                Редактировать
            </button>
        </c:if>
    </div>
</form:form>


</body>
</html>