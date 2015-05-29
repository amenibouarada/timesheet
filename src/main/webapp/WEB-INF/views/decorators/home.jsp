<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="../includes/cache.jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8">

        <c:if test="${NoPageFormat == null}">
            <%@ include file="../includes/meta.jsp" %>
            <%@ include file="../includes/style.jsp" %>
            <%@ include file="../includes/js.jsp" %>
        </c:if>
        <c:choose>
            <c:when test="${NoPageFormat == null}">
                <title><decorator:title default="TimeSheet"/></title>
            </c:when>
            <c:otherwise>
                <title><decorator:title default="Отчет"/></title>
            </c:otherwise>
        </c:choose>

        <link rel="icon" type="image/icon" href="/resources/ico/favicon.ico"/>
        <decorator:head/>
    </head>

    <body class="tundra">

    <div class="content_wrapper">
        <div class="content_page">
            <c:if test="${NoPageFormat == null}">
                <div id="header">
                    <%@ include file="../includes/header.jsp" %>
                </div>
                <!-- header -->

                <div class="info-gray">
                    <div id="indicator">Ваш браузер не поддерживает cookies!</div>
                </div>
            </c:if>

            <div id="home-content">
                <decorator:body/>
            </div>
            <!-- home-content -->
        </div>

        <c:if test="${NoPageFormat == null}">
            <div id="footer" class="content_footer">
                <%@ include file="../includes/footer.jsp" %>
            </div>
        </c:if>
        <!-- footer -->
    </div>
    </body>

</html>