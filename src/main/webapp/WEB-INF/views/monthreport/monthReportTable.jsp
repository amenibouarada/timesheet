<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<sec:authorize access="hasRole('ROLE_ADMIN')">
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


<table  data-dojo-id="monthReportTable" data-dojo-type="dojox.grid.DataGrid" height="500px" autoWidth="true"
        onApplyEdit="monthReportTable_cellChanged" >
    <thead>
    <tr>
        <th field="employee"         width="150px"   >Сотрудник</th>
        <th field="division"         width="150px"   >Подразделение</th>
        <th field="region"           width="100px"   >Регион</th>


<%-- 1  --%>        <%--<th field="employee"   rowspan="2"    width="150px"     >Сотрудник</th>--%>
<%-- 2  --%>        <%--<th field="division"   rowspan="2"    width="150px"     >Подразделение</th>--%>
<%-- 3  --%>        <%--<th field="region"     rowspan="2"    width="100px"     >Регион</th>--%>
                   <%--<th field="ts_month"   colspan="12"                     >В табель за месяц</th>--%>
                   <%--<th field="calc_month" colspan="15"                     >Расчетные показатели за месяц</th>--%>
                <%--</tr>--%>
                <%--<tr>--%>

<%-- 4  --%>        <th field="ts_worked"              width="50px" title="Количество отработанных дней (фактическое)"         editable="true" formatter="monthReportTable_colorCell" >Отработано</th>
<%-- 5  --%>        <th field="ts_vacation"            width="50px" title="Количество дней отпуска"                             >Отпуск</th>
<%-- 6  --%>        <th field="ts_illness"             width="50px" title="Количество дней болезни"                            editable="true" formatter="monthReportTable_colorCell"         >Больничный</th>
<%-- 7  --%>        <th field="ts_all_paid"            width="50px" title="Количество оплаченных дней"                          >Всего оплачено</th>
<%-- 8  --%>        <th field="ts_over_val_fin_comp"   width="50px" title="Переработки подтвержденные финансовой компенсацией" editable="true" formatter="monthReportTable_colorCell"  >Переработки - фин. компенсация</th>
<%-- 9  --%>        <th field="ts_over_accounted"      width="50px" title="Учтенные переработки"                                >Переработки</th>
<%-- 10 --%>        <th field="ts_premium"             width="50px" title="Учтенные переработки - премии"                       >Премии</th>
<%-- 11 --%>        <th field="ts_all_over_accounted"  width="50px" title="Общее количество учтенных переработок"               >Всего учтенных переработок</th>
<%-- 12 --%>        <th field="ts_over_done"           width="50px" title="Количество дней переработок подтвержденных отгулом"  >Переработки отгуленные</th>
<%-- 13 --%>        <th field="ts_over_not_done"       width="50px" title="Количество дней переработок доступных для отгула"    >Переработки не отгуленные</th>
<%-- 14 --%>        <th field="ts_over_remain"         width="50px" title="Переработки с учетом прошлых месяцев"                >Переработки оставшиеся</th>
<%-- 15 --%>        <th field="ts_vacation_avail"      width="50px" title="Доступный отпуск"                                   editable="true" >Доступный отпуск</th>

<%-- 16 --%>        <th field="calc_worked_plan"       width="50px" title="Количество отработанных дней (плановое)"                          >Отработано (план)</th>
<%-- 17 --%>        <th field="calc_worked_fact"       width="50px" title="Количество отработанных дней (фактическое)"                       >Отработано (факт)</th>
<%-- 18 --%>        <th field="calc_vacation"          width="50px" title="Количество дней отпуска"                                          >Отпуск</th>
<%-- 19 --%>        <th field="calc_vacation_with"     width="50px" title="Количество дней отпуска с сохранением содержания"                 >Отпуск с сохр.</th>
<%-- 20 --%>        <th field="calc_vacation_without"  width="50px" title="Количество дней отпуска без сохранения содержания"                >Отпуск без сохр.</th>
<%-- 21 --%>        <th field="calc_vacation_hol_paid" width="50px" title="Количество дней отпуска типа <отгул>"                             >Отпуск-отгул</th>
<%-- 22 --%>        <th field="calc_illness"           width="50px" title="Количество рабочих дней болезни за отчетный период"               >Больничный</th>
<%-- 23 --%>        <th field="calc_illness_with"      width="50px" title="Количество дней болезни подтвержденных больничным листом"         >Больничный подтв.</th>
<%-- 24 --%>        <th field="calc_illness_without"   width="50px" title="Количество дней болезни без больничного листа"                    >Больничный без подтв.</th>
<%-- 25 --%>        <th field="calc_over"              width="50px" title="Общее количество переработанных дней"                             >Переработки</th>
<%-- 26 --%>        <th field="calc_over_hol"          width="50px" title="Количество переработанных дней в выходные и праздничные дни"      >Переработки в вых.</th>
<%-- 27 --%>        <th field="calc_over_hol_paid"     width="50px" title="Количество переработанных дней в выходные с компенсацией"         >Переработки в вых. с компенсацией</th>
<%-- 28 --%>        <th field="calc_over_work"         width="50px" title="Количество переработанных дней в рабочие дни отчетного периода"   >Переработки в раб. дни</th>
<%-- 29 --%>        <th field="calc_worked_ill"        width="50px" title="Количество отработанных дней за время болезни"                    >Отработано в больничный</th>
<%-- 30 --%>        <th field="calc_worked_vac"        width="50px" title="Количество отработанных дней в отпускные дни"                     >Отработано в отпуске</th>

    </tr>
    </thead>
</table>

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

    // расскраска ячеек и проверка на существующее значение заполненности таблицы реальными данными, а не автовычисленными
    // и добавляю подсказку
    var monthReportTable_colorCell = function(value, rowIndex, cell) {
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
