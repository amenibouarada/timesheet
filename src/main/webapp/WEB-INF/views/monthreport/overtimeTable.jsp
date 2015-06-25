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
        onclick="overtimeTable_employeeDialogShow()">Добавить сотрудников</button>
<button onclick="overtimeTable_deleteRows()">Удалить выделенные строки</button>
</sec:authorize>

<table data-dojo-id="overtimeTable" data-dojo-type="dojox.grid.DataGrid"
       onApplyEdit="overtimeTable_cellChanged" height="500px" sortInfo="2">
    <thead>
        <tr>
            <th field="id" width="20px"></th>
            <th field="employee"                width="150px"   >Сотрудник</th>
            <th field="division"                width="150px"   >Подразделение</th>
            <th field="region"                  width="100px"   >Регион</th>
            <th field="type"                    width="150px"   >Тип</th>
            <th field="project"                 width="100px"   >Проект/Пресейл</th>
            <th field="overtime"                width="100px"   editable="true" >Переработки</th>
            <th field="premium"                 width="100px"   editable="true" >Премия</th>
            <th field="allAccountedOvertime"    width="110px"   >Всего учтенных переработок и премий</th>
            <th field="comment"                 width="200px"   editable="true" >Комментарий</th>
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

        var prevValue;
        var fieldName;
        overtimeTable.onStartEdit = function (inCell, inRowIndex) {
            fieldName = inCell.field;
            prevValue = overtimeTable.store.getValue(overtimeTable.getItem(inRowIndex), fieldName);
        }

        overtimeTable.onApplyCellEdit = function (inValue, inRowIndex, inFieldIndex) {
            if (isNaN(Number(inValue))) {
                overtimeTable.store.setValue(overtimeTable.getItem(inRowIndex), fieldName, prevValue);
            }
        }
    });

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
        dojo.xhrPost({
            url: "monthreport/getOvertimes",
            content: {
                divisionOwner: divisionOwner,
                divisionEmployee: divisionEmployee,
                year: year,
                month: month
            },
            handleAs: "text",
            load: function (response, ioArgs) {
                stopProcessing();
                var overtimes = dojo.fromJson(response);
                dojo.forEach(overtimes, function(overtime){
                    // уникальный идентификатор, для добавления новых строк
                    overtime.identifier = overtime.employeeId + "_" + overtime.projectId;
                });
                overtimeTable_addRows(overtimes);
                overtimeTable.store.save();
            },
            error: function (response, ioArgs) {
                stopProcessing();
                alert(response);
            }
        });
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
        dojo.xhrPost({
            url: "monthreport/deleteOvertimes",
            content: {
                jsonData: "[" + jsonData + "]"
            },
            handleAs: "text",
            load: function (response, ioArgs) {
                alert(response);
                if(items.length){
                    dojo.forEach(items, function(selectedItem){
                        if(selectedItem !== null){
                            overtimeTable.store.deleteItem(selectedItem);
                        }
                        overtimeTable.store.save();
                    });
                }
                stopProcessing();
            },
            error: function (response, ioArgs) {
                stopProcessing();
                alert(response);
            }
        });
    }

    function overtimeTable_save(){
        overtimeTable.store.fetch({query: {}, queryOptions: {deep: true},
            onComplete: function (items) {
                overtimeTable.store.save();
                var jsonData = itemToJSON(overtimeTable.store, items);
                dojo.xhrPost({
                    url: "monthreport/saveOvertimeTable",
                    content: {
                        year: dojo.byId("monthreport_year").value,
                        month: dojo.byId("monthreport_month").value,
                        jsonData: "[" + jsonData + "]"
                    },
                    handleAs: "text",
                    load: function (response, ioArgs) {
                        alert(response);
                        overtimeTable_reloadTable();
                    },
                    error: function (response, ioArgs) {
                        alert(response);
                    }
                });
            }
        });
    }

    var overtimeTable_cellChanged = function(rowIndex){
        var item = overtimeTable.getItem(rowIndex);
        overtimeTable.store.setValue(item, "allAccountedOvertime", parseInt(item.overtime) + parseInt(item.premium));
    }

    // обрабатывает кнопку "Добавить" на форме "Добавить сотрудника"
    function returnEmployees(){
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
                employeeId: parseInt(employee.value),
                employee:   employee.innerHTML,
                divisionId: parseInt(employee.attributes.div_id.value),
                division:   employee.attributes.div_name.value,
                regionId:   parseInt(employee.attributes.reg_id.value),
                region:     employee.attributes.reg_name.value,
                typeId:     typeId,
                type:       type,
                projectId:  projectId,
                project:    project,
                overtime:   0.0,
                premium:    0.0,
                allAccountedOvertime: 0.0,
                comment:    ""
            });
        });
        overtimeTable_addRows(employee_list);
    }

</script>