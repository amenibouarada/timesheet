<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
<table class="dijitDialogPaneContentArea no_border employmentPlanningTable">
    <tr>
        <td><label>Подразделение владельца</label></td>
        <td>
            <select data-dojo-id="overtimeTable_divisionOwnerId" id="overtimeTable_divisionOwnerId"
                    onchange="overtimeTable_divisionChanged();">
                <option value="0" label="Все">Все</option>
                <c:forEach items="${divisionList}" var="division">
                    <option value="${division.id}" label="${division.name}">${division.name}</option>
                </c:forEach>
            </select>
        <td>
    </tr>
    <tr>
        <td><label>Подразделение сотрудника</label></td>
        <td>
            <select data-dojo-id="overtimeTable_divisionEmployeeId" id="overtimeTable_divisionEmployeeId"
                    onchange="overtimeTable_divisionChanged()">
                <option value="0" label="Все">Все</option>
                <c:forEach items="${divisionList}" var="division">
                    <option value="${division.id}" label="${division.name}">${division.name}</option>
                </c:forEach>
            </select>
        <td>
    </tr>
</table>

<button data-dojo-id="overtimeTable_addEmployeesButton" id="overtimeTable_addEmployeesButton"
        onclick="overtimeTable_addNewEmployees()">Добавить сотрудников</button>
<button onclick="overtimeTable_deleteRows()">Удалить выделенные строки</button>
</sec:authorize>

<table data-dojo-id="overtimeTable" data-dojo-type="dojox.grid.DataGrid"
       onApplyEdit="overtimeTable_cellChanged" height="500px" sortInfo="1">
    <thead>
        <tr>
            <th field="employee_name"                width="150px"                                                       >Сотрудник</th>
            <th field="division_employee_name"       width="150px"                                                       >Подразделение</th>
            <th field="region_name"                  width="100px"                                                       >Регион</th>
            <th field="project_type_name"            width="100px"                                                       >Тип</th>
            <th field="project_name"                 width="100px"                                                       >Проект/Пресейл</th>
            <th field="overtime"                     width="50px"   editable="true" formatter= "overtimeTable_colorCell" >Переработки</th>
            <th field="premium"                      width="50px"   editable="true" formatter= "overtimeTable_colorCell" >Премия</th>
            <th field="total_accounted_overtime"     width="50px"                                                        >Всего учтенных переработок и премий</th>
            <th field="comment"                      width="100px"  editable="true"                                      >Комментарий</th>
        </tr>
    </thead>
</table>

<%-- Форма добавления сотрудников --%>
<%@include file="../components/addEmployees/addEmployeesForm.jsp" %>

<script type="text/javascript">

    dojo.addOnLoad(function(){
        overtimeTable_createStore();
        if (dojo.byId("overtimeTable_divisionOwnerId")){
            var div = getCookieValue('aplanaDivision');
            div = div ? div : 0;
            dojo.byId("overtimeTable_divisionOwnerId").value = div;
            dojo.byId("overtimeTable_divisionEmployeeId").value = div;
            overtimeTable_divisionChanged();
        }

        monthReport_cellsValidator(overtimeTable, "comment");
    });

    function overtimeTable_addNewEmployees(){
        overtimeTable_employeeDialogShow();
        returnEmployees = overtimeTable_returnEmployees;
    }

    // раскраска ячеек и проверка на существующее значение заполненности таблицы реальными данными, а не автовычисленными
    // и добавляю подсказку
    var overtimeTable_colorCell = function(value, rowIndex, cell) {
        var item = overtimeTable.getItem(rowIndex);
        var calculatedValue = overtimeTable.store.getValue(item, cell.field, null);
        var dispValue = "";
        if (value && value != "null"){
            cell.customStyles.push('color:green');
            dispValue = value;
        }else{
            cell.customStyles.push('color:red');
            dispValue = calculatedValue != null ? calculatedValue : 0;
        }
        return "<span title='Значение по умолчанию: " + calculatedValue + "'>" + dispValue + "</span>"
    }

    function overtimeTable_employeeDialogShow(){
        // изменение значений на форме "Добавить сотрудника"
        dojo.byId("addEmployeesForm_divisionOwnerId").value = overtimeTable_divisionOwnerId.value;
        dojo.byId("addEmployeesForm_divisionId").value = overtimeTable_divisionEmployeeId.value;
        // значения изменились - необходимо запустить функции, обработчики изменений
        addEmployeesForm_updateLists();
        // Открыть форму добавления сотрудников
        addEmployeesForm_employeeDialog.show();
    }

    function overtimeTable_divisionChanged(){
        // обновляем таблицу
        overtimeTable_reloadTable();
        // меняем видимость кнопки "Добавить сотрудников"
        if( overtimeTable_divisionOwnerId.value == ALL_VALUE ||
            overtimeTable_divisionEmployeeId.value == ALL_VALUE)
        {
            overtimeTable_addEmployeesButton.disabled = true;
        }else{
            overtimeTable_addEmployeesButton.disabled = false;
        }
    }

    function overtimeTable_createStore(){
        var data = {
            identifier: 'identifier',
            items: []
        };
        var store = new dojo.data.ItemFileWriteStore({data: data});
        overtimeTable.setStore(store);
    }

    function overtimeTable_reloadTable(){
        if (overtimeTable.store.isDirty()){
            if ( ! confirm("В таблице были изменения. Вы уверены, что хотите обновить данные не записав текущие?")){
                return;
            }
        }
        var year = dojo.byId("monthreport_year").value;
        var month = dojo.byId("monthreport_month").value;
        var divisionOwner = dojo.byId("overtimeTable_divisionOwnerId") ? overtimeTable_divisionOwnerId.value : 0;
        var divisionEmployee = dojo.byId("overtimeTable_divisionEmployeeId") ? overtimeTable_divisionEmployeeId.value : 0;

        processing();
        overtimeTable_createStore();
        makeAjaxRequest(
                "<%= request.getContextPath()%>/monthreport/getOvertimes",
                {
                    divisionOwner: divisionOwner,
                    divisionEmployee: divisionEmployee,
                    year: year,
                    month: month
                },
                "json",
                "Во время запроса данных для таблицы 'Переработки' произошла ошибка. Пожалуйста, свяжитесть с администраторами системы.",
                function (data) {
                    stopProcessing();
                    dojo.forEach(data, function(overtime){
                        // уникальный идентификатор, для добавления новых строк
                        overtime.identifier = overtime.employee_id + "_" + overtime.project_id;
                    });
                    overtimeTable_addRows(data);
                    overtimeTable.store.save();
                }
        );
    }

    function overtimeTable_addRows(overtime_list){
        for(var i=0; i < overtime_list.length; i++){
            var newItem = overtime_list[i];
            overtimeTable.store.fetch( {query: {identifier: newItem.identifier}, queryOptions: {deep: true},
                onComplete: function (items) {
                    if (items.length == 0){ // ранее не было, добавляем
                        overtimeTable.store.newItem(newItem);
                        return;
                    }
                }
            }) ;
        }
    }

    function overtimeTable_deleteRows(){
        var items = overtimeTable.selection.getSelected();
        var jsonData = itemToJSON(overtimeTable.store, items);

        processing();
        makeAjaxRequest(
                "<%= request.getContextPath()%>/monthreport/deleteOvertimes",
                {
                    jsonData: "[" + jsonData + "]"
                },
                "text",
                "Во время удаления данных из таблицы 'Переработки' произошла ошибка. Пожалуйста, свяжитесть с администраторами системы.",
                function () {
                    if(items.length){
                        dojo.forEach(items, function(selectedItem){
                            if(selectedItem !== null){
                                overtimeTable.store.deleteItem(selectedItem);
                            }
                            overtimeTable.store.save();
                        });
                    }
                    stopProcessing();
                }
        );
    }

    function overtimeTable_save(){
        processing();
        overtimeTable.store.fetch({query: {}, queryOptions: {deep: true},
            onComplete: function (items) {
                overtimeTable.store.save();
                var jsonData = itemToJSON(overtimeTable.store, items);
                makeAjaxRequest(
                        "<%= request.getContextPath()%>/monthreport/saveOvertimeTable",
                        {
                            year: dojo.byId("monthreport_year").value,
                            month: dojo.byId("monthreport_month").value,
                            jsonData: "[" + jsonData + "]"
                        },
                        "text",
                        "Во время сохранения таблицы 'Переработки' произошла ошибка. Пожалуйста, свяжитесть с администраторами системы.",
                        function () {
                            stopProcessing();
                            overtimeTable_reloadTable();
                        }
                );
            }
        });
    }

    var overtimeTable_cellChanged = function(rowIndex){
        var item = overtimeTable.getItem(rowIndex);
        overtimeTable.store.setValue(item, "allAccountedOvertime", parseInt(item.overtime) + parseInt(item.premium));
    }

    // обрабатывает кнопку "Добавить" на форме "Добавить сотрудника"
    var overtimeTable_returnEmployees = function(){
        var typeSelect      = dojo.byId("addEmployeesForm_projectTypeId");
        var typeId          = parseInt(typeSelect.value);
        var type            = typeSelect.options[typeSelect.selectedIndex].text;
        var projectSelect   = dojo.byId("addEmployeesForm_projectId");
        var projectId       = projectSelect.value;
        var project = "";
        if (projectId != ""){
            project = projectSelect.options[projectSelect.selectedIndex].text;
            projectId = parseInt(projectId);
        }else{
            projectId = null;
        }
        var employee_list = [];
        var length = 0;
        dojo.forEach( dojo.byId("addEmployeesForm_additionEmployeeList").selectedOptions, function(employee){
            employee_list.push({
                identifier: employee.value + "_" + projectId, // уникальный идентификатор, для добавления новых строк
                id:         null,
                employee_id: parseInt(employee.value),
                employee_name:   employee.innerHTML,
                division_employee_id: parseInt(employee.attributes.div_id.value),
                division_employee_name:   employee.attributes.div_name.value,
                region_id:   parseInt(employee.attributes.reg_id.value),
                region_name:     employee.attributes.reg_name.value,
                project_type_name:   typeId,
                project_type_id:   type,
                project_id:  projectId,
                project_name:    project,
                overtime:   0.0,
                premium:    0.0,
                total_accounted_overtime: 0.0,
                comment:    ""
            });
        });
        overtimeTable_addRows(employee_list);
    }

</script>