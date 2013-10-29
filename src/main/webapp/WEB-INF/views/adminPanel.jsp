<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>

<html>
    <head>
        <title><fmt:message key="title.adminpanel"/></title>
        <script type="text/javascript">
            window.onload = function () {
                var updatePropertiesLink = document.getElementById("updateProperties");
                updatePropertiesLink.href = "#";
                updatePropertiesLink.onclick = function () {
                    var text = updatePropertiesLink.innerHTML;
                    updatePropertiesLink.innerHTML = "<img src=\"<c:url value="/resources/img/loading_small.gif"/>\"/>" + text;

                    dojo.xhrGet({
                        url: "<%= request.getContextPath()%>/admin/update/propertiesAJAX",
                        handleAs: "text",

                        load: function (data) {
                            if (data.size == 0) {
                                data = "неизвестно";
                            }
                            showTextMessage("Настройки системы успешно обновлены из файла " + data);
                            updatePropertiesLink.innerHTML = text;
                        },

                        error: function (error) {
                            updatePropertiesLink.setAttribute("class", "error");
                            updatePropertiesLink.innerHTML = error;
                        }
                    });
                }
            };

            function showTextMessage(msg) {
                var messagebox = document.getElementById("messageBox");
                messagebox.innerHTML = "<b>" + msg + "</b>";
            }

            function updateShowUser() {
                var checked = dojo.byId("allUserCheckBox").checked;
                var url = "/admin/update/hidealluser";

                if (checked) {
                    url = "/admin/update/showalluser";
                }

                dojo.xhrGet({
                    url: url
                });
            }

        </script>
    </head>

    <body>
        <h1><fmt:message key="title.adminpanel"/></h1>

        <br/>

        <div id="messageBox"></div>
        <input type="checkbox" name="showAllUser" id="allUserCheckBox" onChange="updateShowUser();"
               <c:if test="${showalluser == true}">checked="checked"</c:if>
                > <fmt:message key="link.showalluser"/>
        <br/>
        <br/>
        <ul>
            <li><a href="admin/update/ldap"><fmt:message key="link.updateldap"/></a></li>
            <li><a href="admin/update/checkreport"><fmt:message key="link.checkemails"/></a></li>
            <li><a href="admin/update/oqsync"><fmt:message key="link.oqsync"/></a></li>
            <li><a href="admin/update/properties" id="updateProperties"><fmt:message key="link.update.properties"/></a>
            </li>
            <li><a href="admin/update/siddisabledusersfromldap"><fmt:message key="link.disabledsidsync"/></a></li>
            <li><a href="admin/update/sidallusersfromldap"><fmt:message key="link.allsidsync"/></a></li>
            <li><a href="admin/update/jiranameallusersfromldap"><fmt:message key="link.alljiranamesync"/></a></li>

        </ul>

    </body>
</html>