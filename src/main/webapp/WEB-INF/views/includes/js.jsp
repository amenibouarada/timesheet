<%@ page import="static com.aplana.timesheet.system.constants.TimeSheetConstants.DOJO_PATH" %>
<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@ page import="com.aplana.timesheet.enums.TypesOfActivityEnum" %>
<%@ page import="com.aplana.timesheet.enums.VacationTypesEnum" %>
<!-- load Dojo -->
<%!
    private static final String DATE_TEXT_BOX_EXT_JS_PATH = "/resources/js/DateTextBox.ext.js";
    private static final String CALENDAR_EXT_RES_PATH = "/resources/js/Calendar.ext.js";
%>

<script type="text/javascript">
    var dojoConfig = {
        parseOnLoad: true,
        locale:'ru'
    };

    window.EnumConstants = {
        TypesOfActivityEnum: {<%for (TypesOfActivityEnum name: TypesOfActivityEnum.values()) {
                out.print(String.format("%s : %s,\n", name.name(), name.getId()));}%>},
        VacationTypesEnum: {<%for (VacationTypesEnum name: VacationTypesEnum.values()) {
                out.print(String.format("%s : %s,\n", name.name(), name.getId()));}%>}
    }
</script>

<script type="text/javascript" src="<%=request.getContextPath()%><%= DOJO_PATH %>/dojo/dojo.js"></script>

<script type="text/javascript" src="<%= getResRealPath("/resources/js/dformat.js", application) %>"></script>
<script type="text/javascript" src="<%= getResRealPath("/resources/js/utils.js", application) %>"></script>

<script type="text/javascript" src="<%= getResRealPath(CALENDAR_EXT_RES_PATH, application) %>"></script>
<script type="text/javascript" src="<%= getResRealPath(DATE_TEXT_BOX_EXT_JS_PATH, application) %>"></script>

<script type="text/javascript">
    function getContextPath() {
        return "<%= request.getContextPath() %>";
    }
</script>