<%@ page import="java.io.File" %><%
long modifiedStyleCss = new File(application.getRealPath("/resources/css/style.css")).lastModified();
long modifiedMenuCss = new File(application.getRealPath("/resources/css/menu.css")).lastModified();
out.print("<link href=\"" + request.getContextPath() + "/resources/js/dojo-release-1.8.3/dijit/themes/tundra/tundra.css\" rel=\"stylesheet\" type=\"text/css\" />");
out.print("<link href=\"" + request.getContextPath() + "/resources/js/dojo-release-1.8.3/dojo/resources/dojo.css\" rel=\"stylesheet\" type=\"text/css\" />");

out.print("<link href=\"" + request.getContextPath() + "/resources/css/style.css?modified=" + modifiedStyleCss + "\" rel=\"stylesheet\" type=\"text/css\" />");
out.print("<link href=\"" + request.getContextPath() + "/resources/css/menu.css?modified=" + modifiedMenuCss + "\" rel=\"stylesheet\" type=\"text/css\" />");

%>