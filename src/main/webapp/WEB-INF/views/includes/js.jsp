<%@ page import="static com.aplana.timesheet.system.constants.TimeSheetConstants.DOJO_PATH" %>
<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@ page import="com.aplana.timesheet.enums.TypesOfActivityEnum" %>
<%@ page import="com.aplana.timesheet.enums.VacationTypesEnum" %>

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

<%--TODO возможно при сборке надо объединять в один файл, чтобы клиент скачивал только один файл, а не 5 --%>
<script type="text/javascript" src="<%= getResRealPath("/resources/js/utils/dformat.js", application) %>"></script>
<script type="text/javascript" src="<%= getResRealPath("/resources/js/utils/commonUtils.js", application) %>"></script>
<script type="text/javascript" src="<%= getResRealPath("/resources/js/utils/selectWidgetsUtils.js", application) %>"></script>
<script type="text/javascript" src="<%= getResRealPath("/resources/js/DateTextBox.ext.js", application) %>"></script>
<script type="text/javascript" src="<%= getResRealPath("/resources/js/Calendar.ext.js", application) %>"></script>

<script type="text/javascript">
    function getContextPath() {
        return "<%= request.getContextPath() %>";
    }
</script>