<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<script type="text/javascript" src="/resources/js/header.js"></script>
<script type="text/javascript" src="/resources/js/marquee.js"></script>
<script type="text/javascript">
    dojo.addOnLoad(function () {
        <sec:authorize access="isAuthenticated()">
        getVacationsNeedsApprovalCountString();
        getReportOverdueEmployeesNames();
        aplanaMarquee.init("headerMarquee", 3000, 6000);
        </sec:authorize>
    });
</script>

<a href="<c:url value='/'/>"><img id="logo" src="<%=request.getContextPath()%>/resources/img/logo.png"
                                  alt="Aplana Software"/></a>

<div id="header_text">
    Корпоративная система списания занятости
    <div id="headerMarquee">
        <div id="headerMarqueeContent">
        </div>
    </div>
</div>
<sec:authorize access="isAuthenticated()">
    <div class="employee_name">
        <sec:authentication property="principal.employee.name"/>
        <span style="border-left: 1px solid #d9d9d9; border-right: 1px solid #ffffff; padding: 0px 5px">
            <a style="text-decoration: none; color: #371C1C;" href="<c:url value='/logout'/>">Выйти</a>
        </span>
    </div>

    <div id="cssmenu">
        <ul>
            <li><a href="<c:url value='/'/>"><fmt:message key="menu.main"/></a></li>

            <li><a href="<c:url value='/viewreports'/>"><fmt:message key="menu.viewreports"/></a></li>

            <sec:authorize access="hasAnyRole('ROLE_PLAN_EDIT', 'ROLE_PLAN_VIEW')">
                <li><a href="<c:url value='/planEdit'/>"><fmt:message key="menu.planEdit"/></a></li>
            </sec:authorize>

            <li><a href="<c:url value='/businesstripsandillness'/>"><fmt:message key="menu.businesstripsandillness"/></a></li>

            <li><a href="<c:url value='/vacations'/>"><fmt:message key="menu.vacations"/><span id="vacationCount"></span></a></li>

            <li><a href="<c:url value='/activeProjects'/>"><fmt:message key="menu.activeProjects"/></a></li>

            <sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')">
                <li><a href="<c:url value='/managertools'/>"><fmt:message key="menu.manager"/></a></li>
            </sec:authorize>

            <sec:authorize access="hasRole('ROLE_ADMIN')">
                <li><a href="<c:url value='/admin'/>"><fmt:message key="menu.admin"/></a></li>
            </sec:authorize>

            <li><a href="<c:url value='/feedback'/>"><fmt:message key="menu.feedback"/></a></li>

            <li><a href="<c:url value='/birthdays'/>"><fmt:message key="menu.birthdays"/></a></li>
        </ul>
    </div>
</sec:authorize>

<div class="clear"></div>