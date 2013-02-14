<%@ page import="static com.aplana.timesheet.constants.TimeSheetConstants.DOJO_PATH" %>
<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<!-- load Dojo -->
<%!
    private static final String DATE_TEXT_BOX_EXT_JS_PATH = "/resources/js/DateTextBox.ext.js";
    private static final String CALENDAR_EXT_RES_PATH = "/resources/js/Calendar.ext.js";
%>

<script
        src="<%=request.getContextPath()%><%= DOJO_PATH %>/dojo/dojo.js"
        djConfig="parseOnLoad: true">
</script>
<script
        src="<%= getResRealPath("/resources/js/timesheet.js", application) %>">
</script>
<script
        src="<%= getResRealPath("/resources/js/dformat.js", application) %>">
</script>

<script type="text/javascript" src="<%= getResRealPath(CALENDAR_EXT_RES_PATH, application) %>"></script>
<script type="text/javascript" src="<%= getResRealPath(DATE_TEXT_BOX_EXT_JS_PATH, application) %>"></script>

<script type="text/javascript">
    function getContextPath() {
        return "<%= request.getContextPath() %>";
    }
</script>