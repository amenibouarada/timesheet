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
                <option value="0" label="Все">Все</option>
                <c:forEach items="${regionList}" var="region">
                    <option value="${region.id}" label="${region.name}">${region.name}</option>
                </c:forEach>
            </select>
        </td>
        <td rowspan="2">
            <label>Должности</label><br>
            <select data-dojo-id="monthReportTable_projectRoleListId" id="monthReportTable_projectRoleListId" multiple="true"
                    onchange="monthReportTable_reloadTable()">
                <option value="0" label="Все">Все должности</option>
                <c:forEach items="${projectRoleList}" var="projectRole">
                    <option value="${projectRole.id}" label="${projectRole.name}">${projectRole.name}</option>
                </c:forEach>
            </select>
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

<div data-dojo-id="monthReportTable" data-dojo-type="dojox.grid.DataGrid" height="430px"
     onApplyEdit="monthReportTable_cellChanged" structure="gridlayout">
</div>

<script type="text/javascript">

    // TODO: Реализовать группировку колонок заголовка в соответствии с обозначенными ниже группами.
    // TODO: Сделать возможность включения групп колонок : по умолчанию включена группа Управленческий табель, все друугие выключены

    var gridlayout = [
        {
            cells: [[
/* 0  */                {field: "employee", name: "Сотрудник", width: "120px"}
            ]], noscroll: true
        },
        {
            cells: [[

/* 1  */     {field: "division"                 , name: "Подразделение",                                     width: "162px"},
/* 2  */     {field: "region"                   , name: "Регион",                                            width: "100px"},

                    // Группа "Управленческий табель"

/* 3  */     {field: "ts_worked"                , name: "Отработано",                                        width: "50px", editable: true, formatter: monthReport_colorCell},
/* 4  */     {field: "overtimes_paid_current"   , name: "Оплач. переработки этого месяца",                   width: "50px"},
/* 5  */     {field: "overtimes_paid_previous"  , name: "Оплач. переработки пред.периодов",                  width: "50px", editable: true},
/* 6  */     {field: "calc_vacation_with"       , name: "Отпуск с сохранением фактический",                  width: "50px"},
/* 7  */     {field: "calc_vacation_without"    , name: "Отпуск без сохранения",                             width: "50px"},
/* 8  */     {field: "calc_vacation_hol_paid"   , name: "Переработки, отгуленные в этом месяце",             width: "50px"},
/* 9  */     {field: "ts_illness"               , name: "Больничные дни за этот месяц",                      width: "50px", editable: true, formatter: monthReport_colorCell},
/* 10 */     {field: "ts_all_paid"              , name: "Всего оплачено рабочих дней",                       width: "50px"},
/* 11 */     {field: "ts_all_over_accounted"    , name: "Всего оплачено переработок",                        width: "50px"},

                    // Группа "Отпуска и отгулы"

/* 12 */     {field: "ts_vacation_avail"        , name: "Доступный на конец месяца отпуск",                  width: "50px", editable: true},
/* 13 */     {field: "ts_over_remain"           , name: "Доступные на конец месяца для отгула переработки",  width: "50px", editable: true, formatter: monthReport_colorCell},
/* 14 */     {field: "calc_worked_vac"          , name: "Работа в отпуске/отгуле в этом месяце",             width: "50px"},
/* 15 */     {field: "ts_vacation"              , name: "Отпуск, начисленный в этом месяце",                 width: "50px"},
/* 16 */     {field: "overtimes_acc_current"    , name: "Перер, начисленные в отгул в этом месяце",          width: "50px"},

                    // Группа "Больничные"

/* 17 */     {field: "calc_illness"             , name: "Больничные итого",                                  width: "50px"},
/* 18 */     {field: "calc_illness_with"        , name: "Больничные с подтв.",                               width: "50px"},
/* 19 */     {field: "calc_illness_without"     , name: "Больничные без подтв.",                             width: "50px"},
/* 20 */     {field: "calc_worked_ill"          , name: "Работа на больничном в этом месяце",                width: "50px"},


                    // Группа Расчетные показатели по отработанным дням

/* 20 */     {field: "calc_worked_plan"         , name: "Отработано (план)",                                 width: "50px"},
/* 21 */     {field: "calc_worked_fact"         , name: "Отработано (факт)",                                 width: "50px"}

            ]]
        }
    ];

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


    // Функция для назначения видимости/невидимости определённых колонок
    function setColumnVisibility(visible, startIndex, endIndex){
        for (var i = startIndex; i <= endIndex; i++){
            monthReportTable.layout.setColumnVisibility(/* int */ i - 1, /* bool */ visible);
        }
    }

    function monthReportTable_reloadTable(){
        //дизактивируем кнопку "Сохранить"
        monthReport_saveButtonChangeState(false);
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
        makeAjaxRequest(
                "<%= request.getContextPath()%>/monthreport/getMonthReportData",
                {
                    division: divisionId,
                    manager: managerId,
                    regions: regions,
                    roles: roles,
                    year: year,
                    month: month
                },
                "json",
                "Во время запроса данных для табеля произошла ошибка. Пожалуйста, свяжитесть с администраторами системы.",
                function (data) {
                    fillStore(monthReportTable, data);
                }
        );
    }

    function monthReportTable_save(){
        monthReportTable.store.fetch({query: {}, queryOptions: {deep: true},
            onComplete: function (items) {
                monthReportTable.store.save();
                var jsonData = itemToJSON(monthReportTable.store, items);
                makeAjaxRequest(
                        "<%= request.getContextPath()%>/monthreport/saveMonthReportTable",
                        {
                            year: dojo.byId("monthreport_year").value,
                            month: dojo.byId("monthreport_month").value,
                            jsonData: "[" + jsonData + "]"
                        },
                        "text",
                        "Во время сохранения табеля произошла ошибка. Пожалуйста, свяжитесть с администраторами системы.",
                        function () {
                            monthReportTable_reloadTable();
                            monthReport_updateStatus();
                        }
                );
            }
        });
    }

    var monthReportTable_cellChanged = function(rowIndex){
        var item = monthReportTable.getItem(rowIndex);
        if (item.ts_worked[0] && item.ts_worked[0] != "null"){ // проверка, что поле, от которого зависит значение - содержит реальное значение, а не по умолчанию
            monthReportTable.store.setValue(item, "ts_all_paid", (parseFloat(item.ts_worked) + parseFloat(item.ts_vacation)).toPrecision(3));
        }
    }

</script>
