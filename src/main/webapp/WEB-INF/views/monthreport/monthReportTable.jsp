<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<table class="dijitDialogPaneContentArea no_border employmentPlanningTable">
    <tr>
        <td><label>Подразделение</label></td>
        <td>
            <select data-dojo-id="monthReportTable_divisionId" id="monthReportTable_divisionId"
                    onchange="monthReportTable_updateManagers(); monthReportTable_reloadTable();">
                <option value="0" label="Все">
                <c:forEach items="${divisionList}" var="division">
                    <option value="${division.id}" label="${division.name}">
                </c:forEach>
            </select>
        <td>
        <td rowspan="2">
            <label>Регионы </label><br>
            <select data-dojo-id="monthReportTable_regionListId" id="monthReportTable_regionListId" multiple="true"
                    onchange="monthReportTable_reloadTable()">
                <c:forEach items="${regionList}" var="region">
                <option value="${region.id}" label="${region.name}">
                    </c:forEach>
            </select>
        </td>
        <td rowspan="2">
            <label>Должности</label><br>
            <select data-dojo-id="monthReportTable_projectRoleListId" id="monthReportTable_projectRoleListId" multiple="true"
                    onchange="monthReportTable_reloadTable()">
                <c:forEach items="${projectRoleList}" var="projectRole">
                <option value="${projectRole.id}" label="${projectRole.name}">
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


<table  data-dojo-id="monthReportTable" data-dojo-type="dojox.grid.DataGrid" autoHeight="true">
    <thead>
    <tr>

        <th field="id" rowspan="2" width="20px">
            <%--<img src="<c:url value='/resources/img/add.gif'/>" class="create_img" title="Создать" onclick="employeeDialog.show();"/>--%>
        </th>
        <th field="employee" rowspan="2"               width="150px"   >Сотрудник</th>
        <th field="division" rowspan="2"               width="150px"   >Подразделение</th>
        <th field="region"   rowspan="2"               width="100px"   >Регион</th>
        <th field="ts_month"   colspan="12" >В табель за месяц</th>
        <th field="calc_month" colspan="15" >Расчетные показатели за месяц</th>
    </tr>
    <tr>
        <th field="ts_worked"              title="Количество отработанных дней (фактическое)"          >Отработано</th>
        <th field="ts_vacation"            title="Количество дней отпуска"                             >Отпуск</th>
        <th field="ts_illness"             title="Количество дней болезни"                             >Больничный</th>
        <th field="ts_all_paid"            title="Количество оплаченных дней"                          >Всего оплачено</th>
        <th field="ts_over_val_fin_comp"   title="Переработки подтвержденные финансовой компенсацией"  >Переработки - фин. компенсация</th>
        <th field="ts_over_accounted"      title="Учтенные переработки"                                >Переработки</th>
        <th field="ts_premium"             title="Учтенные переработки - премии"                       >Премии</th>
        <th field="ts_all_over_accounted"  title="Общее количество учтенных переработок"               >Всего учтенных переработок</th>
        <th field="ts_over_done"           title="Количество дней переработок подтвержденных отгулом"  >Переработки отгуленные</th>
        <th field="ts_over_not_done"       title="Количество дней переработок доступных для отгула"    >Переработки не отгуленные</th>
        <th field="ts_over_remain"         title="Переработки с учетом прошлых месяцев"                >Переработки оставшиеся</th>
        <th field="ts_vacation_avail"      title="Доступный отпуск"                                    >Доступный отпуск</th>

        <th field="calc_worked_plan"       title="Количество отработанных дней (плановое)"                          >Отработано (план)</th>
        <th field="calc_worked_fact"       title="Количество отработанных дней (фактическое)"                       >Отработано (факт)</th>
        <th field="calc_vacation"          title="Количество дней отпуска"                                          >Отпуск</th>
        <th field="calc_vacation_with"     title="Количество дней отпуска с сохранением содержания"                 >Отпуск с сохр.</th>
        <th field="calc_vacation_without"  title="Количество дней отпуска без сохранения содержания"                >Отпуск без сохр.</th>
        <th field="calc_vacation_hol_paid" title="Количество дней отпуска типа <отгул>"                             >Отпуск-отгул</th>
        <th field="calc_illness"           title="Количество рабочих дней болезни за отчетный период"               >Больничный</th>
        <th field="calc_illness_with"      title="Количество дней болезни подтвержденных больничным листом"         >Больничный подтв.</th>
        <th field="calc_illness_without"   title="Количество дней болезни без больничного листа"                    >Больничный без подтв.</th>
        <th field="calc_over"              title="Общее количество переработанных дней"                             >Переработки</th>
        <th field="calc_over_hol"          title="Количество переработанных дней в выходные и праздничные дни"      >Переработки в вых.</th>
        <th field="calc_over_hol_paid"     title="Количество переработанных дней в выходные с компенсацией"         >Переработки в вых. с компенсацией</th>
        <th field="calc_over_work"         title="Количество переработанных дней в рабочие дни отчетного периода"   >Переработки в раб. дни</th>
        <th field="calc_worked_ill"        title="Количество отработанных дней за время болезни"                    >Отработано в больничный</th>
        <th field="calc_worked_vac"        title="Количество отработанных дней в отпускные дни"                     >Отработано в отпуске</th>

    </tr>
    </thead>
</table>

<script type="text/javascript">

    dojo.addOnLoad(function(){
        monthReportTable_updateManagers();
        monthReportTable_createStore();

        var div = getCookieValue('aplanaDivision');
        div = div ? div : 0;
        monthReportTable_divisionId.value = div;
    });

    function monthReportTable_createStore(){
        var data = {
            identifier: 'identifier',
            items: []
        };
        var store = new dojo.data.ItemFileWriteStore({data: data});
        monthReportTable.setStore(store);
    }

    function monthReportTable_updateManagers(){
        updateManagerListByDivision(
            0, managerMapJson, dojo.byId("monthReportTable_divisionId"), dojo.byId("monthReportTable_managerId"));
    }

    function monthReportTable_reloadTable(){
        dojo.xhrPost({
            url: "monthreport/getMonthReportData",
            content: {
//                divisionOwner: divisionOwner,
//                divisionEmployee: divisionEmployee,
//                year: year,
//                month: month
            },
            handleAs: "text",
            load: function (response, ioArgs) {
//                var overtimes = dojo.fromJson(response);
//                dojo.forEach(overtimes, function(overtime){
//                    // уникальный идентификатор, для добавления новых строк
//                    overtime.identifier = overtime.employeeId + "_" + overtime.projectId;
//                });
//                overtimeTable_addRows(overtimes);
//                overtimeTable.store.save();
            },
            error: function (response, ioArgs) {
//                alert(response);
            }
        });

    }

</script>
