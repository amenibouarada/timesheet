<%@ page import="com.aplana.timesheet.system.properties.TSPropertyProvider" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    String version = TSPropertyProvider.getFooterText();
    String help = TSPropertyProvider.getTimesheetHelpUrl();
%>
<div id="footer_wrapper">
    <p style="text-align: center">
        <script type="text/javascript">
            var ua = navigator.userAgent.toLowerCase();
            if (ua.indexOf("gecko") == -1 && ua.indexOf("chrome")) {
                document.write("<fmt:message key="recomendation.browser.using.text"/>" + "<br>");
            }
        </script>
        <%= version %> <br>
        <a href=<%= help %>><fmt:message key="help.text"/></a>
    </p>
</div>