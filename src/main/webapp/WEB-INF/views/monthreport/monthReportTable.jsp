<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
<table class="dijitDialogPaneContentArea no_border employmentPlanningTable">
    <tr>
        <td><label>Подразделение</label></td>
        <td>
            <select data-dojo-id="monthReportTable_divisionId" id="monthReportTable_divisionId"
                    onchange="monthReportTable_updateManagers(); monthReportTable_reloadTable();">
                <option value="0" label="Все">Все</option>
                <c:forEach items="${divisionList}" var="division">
                    <option value="${division.id}" label="${division.name}">${division.name}</option>
                </c:forEach>
            </select>
        <td>
        <td rowspan="2">
            <label>Регионы </label><br>
            <select data-dojo-id="monthReportTable_regionListId" id="monthReportTable_regionListId" multiple="true"
                    onchange="monthReportTable_reloadTable()">
                <c:forEach items="${regionList}" var="region">
                    <option value="${region.id}" label="${region.name}">${region.name}</option>
                </c:forEach>
            </select>
        </td>
        <td rowspan="2">
            <label>Должности</label><br>
            <select data-dojo-id="monthReportTable_projectRoleListId" id="monthReportTable_projectRoleListId" multiple="true"
                    onchange="monthReportTable_reloadTable()">
                <c:forEach items="${projectRoleList}" var="projectRole">
                    <option value="${projectRole.id}" label="${projectRole.name}">${projectRole.name}</option>
                </c:forEach>
            </select>
        </td>
        <td>
            <input type="checkbox" checked="true" onclick="setCalcMonthColumnVisibility(this.checked)">
            <label>Показывать рассчетные показатели</label>
        </td>
    </tr>
    <tr>
        <td><label>Руководитель</label></td>
        <td>
            <select data-dojo-id="monthReportTable_managerId" id="monthReportTable_managerId" onchange="monthReportTable_reloadTable()">
            </select>
        <td>
    </tr>
</table>
</sec:authorize>

<div data-dojo-id="monthReportTable" data-dojo-type="dojox.grid.DataGrid" height="500px"
     onApplyEdit="monthReportTable_cellChanged" structure="gridlayout">
</div>

<script type="text/javascript">

    dojo.addOnLoad(function(){
        monthReportTable_createStore();
        if (dojo.byId("monthReportTable_divisionId")){
            monthReportTable_updateManagers();
            var div = getCookieValue('aplanaDivision');
            div = div ? div : 0;
            monthReportTable_divisionId.value = div;
        }
    });

    var gridlayout = [
        {
            cells: [[
/* 0  */                {field: "employee", name: "Сотрудник", width: "150px"}
            ]], noscroll: true
        },
        {
            cells: [[
/* 1  */                {field: "division"              , name: "Подразделение",                    width: "180px"},
/* 2  */                {field: "region"                , name: "Регион",                           width: "100px"},
/* 3  */                {field: "ts_worked"             , name: "Отработано",                       width: "50px", editable: true, formatter: monthReportTable_colorCell},
/* 4  */                {field: "ts_vacation"           , name: "Отпуск",                           width: "50px"},
/* 5  */                {field: "ts_illness"            , name: "Больничный",                       width: "50px", editable: true, formatter: monthReportTable_colorCell},
/* 6  */                {field: "ts_all_paid"           , name: "Всего оплачено",                   width: "50px"},
/* 7  */                {field: "ts_over_val_fin_comp"  , name: "Переработки - фин. компенсация",   width: "50px", editable: true, formatter: monthReportTable_colorCell},
/* 8  */                {field: "ts_over_accounted"     , name: "Переработки",                      width: "50px"},
/* 9  */                {field: "ts_premium"            , name: "Премии",                           width: "50px"},
/* 10 */                {field: "ts_all_over_accounted" , name: "Всего учтенных переработок",       width: "50px"},
/* 11 */                {field: "ts_over_done"          , name: "Переработки отгуленные",           width: "50px"},
/* 12 */                {field: "ts_over_not_done"      , name: "Переработки не отгуленные",        width: "50px"},
/* 13 */                {field: "ts_over_remain"        , name: "Переработки оставшиеся",           width: "50px"},
/* 14 */                {field: "ts_vacation_avail"     , name: "Доступный отпуск",                 width: "50px"},
/* 15 */                {field: "calc_worked_plan"      , name: "Отработано (план)",                width: "50px"},
/* 16 */                {field: "calc_worked_fact"      , name: "Отработано (факт)",                width: "50px"},
/* 17 */                {field: "calc_vacation"         , name: "Отпуск",                           width: "50px"},
/* 18 */                {field: "calc_vacation_with"    , name: "Отпуск с сохр.",                   width: "50px"},
/* 19 */                {field: "calc_vacation_without" , name: "Отпуск без сохр.",                 width: "50px"},
/* 20 */                {field: "calc_vacation_hol_paid", name: "Отпуск-отгул",                     width: "50px"},
/* 21 */                {field: "calc_illness"          , name: "Больничный",                       width: "50px"},
/* 22 */                {field: "calc_illness_with"     , name: "Больничный подтв.",                width: "50px"},
/* 23 */                {field: "calc_illness_without"  , name: "Больничный без подтв.",            width: "50px"},
/* 24 */                {field: "calc_over"             , name: "Переработки",                      width: "50px"},
/* 25 */                {field: "calc_over_hol"         , name: "Переработки в вых.",               width: "50px"},
/* 26 */                {field: "calc_over_hol_paid"    , name: "Переработки в вых. с компенсацией",width: "50px"},
/* 27 */                {field: "calc_over_work"        , name: "Переработки в раб. дни",           width: "50px"},
/* 28 */                {field: "calc_worked_ill"       , name: "Отработано в больничный",          width: "50px"},
/* 29 */                {field: "calc_worked_vac"       , name: "Отработано в отпуске",             width: "50px"}
            ]]
        }
    ];

    // расскраска ячеек и проверка на существующее значение заполненности таблицы реальными данными, а не автовычисленными
    // и добавляю подсказку
    function monthReportTable_colorCell(value, rowIndex, cell) {
        var item = monthReportTable.getItem(rowIndex);
        var calculatedValue = monthReportTable.store.getValue(item, cell.field + "_calculated", null);
        var dispValue = "";
        if (value){
            cell.customStyles.push('color:green');
            dispValue = value;
        }else{
            cell.customStyles.push('color:red');
            dispValue = calculatedValue;
        }
        return "<span title='Значение по умолчанию: " + calculatedValue + "'>" + dispValue + "</span>"
    }

    function monthReportTable_createStore(){
        var data = {
            identifier: 'employeeId',
            items: []
        };
        var store = new dojo.data.ItemFileWriteStore({data: data});
        monthReportTable.setStore(store);
    }

    function monthReportTable_updateManagers(){
        updateManagerListByDivision(
                0, managerMapJson, dojo.byId("monthReportTable_divisionId"), dojo.byId("monthReportTable_managerId"));
    }

    function setCalcMonthColumnVisibility(visible){
        for (var i = 16; i <= 30; i++){
            monthReportTable.layout.setColumnVisibility(/* int */ i - 1, /* bool */ visible);
        }
    }

    function monthReportTable_reloadTable(){
        if (monthReportTable.store && monthReportTable.store.isDirty()){
            if ( ! confirm("В таблице были изменения. Вы уверены, что хотите обновить данные не записав текущие?")){
                return;
            }
        }

        var year = dojo.byId("monthreport_year").value;
        var month = dojo.byId("monthreport_month").value;
        var divisionId  = dojo.byId("monthReportTable_divisionId") ?
                monthReportTable_divisionId.value : 0;
        var managerId   = dojo.byId("monthReportTable_managerId") ?
                monthReportTable_managerId.value : 0;
        var regions     = dojo.byId("monthReportTable_regionListId") ?
                "[" + getSelectValues(monthReportTable_regionListId) + "]" : "[]";
        var roles       = dojo.byId("monthReportTable_projectRoleListId") ?
                "[" + getSelectValues(monthReportTable_projectRoleListId) + "]" : "[]";

        monthReportTable_createStore();
        processing();
        dojo.xhrPost({
            url: "monthreport/getMonthReportData",
            content: {
                division: divisionId,
                manager: managerId,
                regions: regions,
                roles: roles,
                year: year,
                month: month
            },
            handleAs: "text",
            load: function (response, ioArgs) {
                var data = dojo.fromJson(response);
                for(var i=0; i < data.length; i++){
                    monthReportTable.store.newItem(data[i]);
                }
                monthReportTable.store.save();
                stopProcessing();
            },
            error: function (response, ioArgs) {
                stopProcessing();
                alert(response);
            }
        });

    }

    function monthReportTable_save(){
        processing();
        monthReportTable.store.fetch({query: {}, queryOptions: {deep: true},
            onComplete: function (items) {
                monthReportTable.store.save();
                var jsonData = itemToJSON(monthReportTable.store, items);
                dojo.xhrPost({
                    url: "monthreport/saveMonthReportTable",
                    content: {
                        year: dojo.byId("monthreport_year").value,
                        month: dojo.byId("monthreport_month").value,
                        jsonData: "[" + jsonData + "]"
                    },
                    handleAs: "text",
                    load: function (response, ioArgs) {
                        stopProcessing();
                        alert(response);
                        monthReportTable_reloadTable();
                        monthReport_updateStatus();
                    },
                    error: function (response, ioArgs) {
                        stopProcessing();
                        alert(response);
                    }
                });
            }
        });
    }

    var monthReportTable_cellChanged = function(rowIndex){
        var item = monthReportTable.getItem(rowIndex);
        if (item.ts_worked[0]){ // проверка, что поле, от которого зависит значение - содержит реальное значение, а не по умолчанию
            monthReportTable.store.setValue(item, "ts_all_paid", parseInt(item.ts_worked) + parseInt(item.ts_vacation));
        }
        if(item.ts_over_val_fin_comp[0]){ // проверка, что поле, от которого зависит значение - содержит реальное значение, а не по умолчанию
            monthReportTable.store.setValue(item, "ts_over_not_done", parseInt(item.ts_all_over_accounted) - parseInt(item.ts_over_done) - parseInt(item.ts_over_val_fin_comp));
        }
    }

</script>
