<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<table class="dijitDialogPaneContentArea no_border employmentPlanningTable">
    <tr>
        <td><label>Подразделение владельца</label></td>
        <td>
            <select data-dojo-id="mutualWorkTable_divisionOwnerId" id="mutualWorkTable_divisionOwnerId"
                    onchange="mutualWorkTable_divisionChanged(); mutualWorkTable_reloadTable();">
                <option value="0" label="Все">Все</option>
                    <c:forEach items="${divisionList}" var="division">
                <option value="${division.id}" label="${division.name}">${division.name}</option>
                </c:forEach>
            </select>
        <td>

        <td rowspan="3">
            <label>Регионы </label><br>
            <select data-dojo-id="mutualWorkTable_regionListId" id="mutualWorkTable_regionListId" multiple="true"
                    onchange="mutualWorkTable_reloadTable()">
                <option value="0" label="Все">Все</option>
                <c:forEach items="${regionList}" var="region">
                    <option value="${region.id}" label="${region.name}">${region.name}</option>
                </c:forEach>
            </select>
        </td>
    </tr>
    <tr>
        <td><label>Проект</label></td>
        <td>
            <select id="mutualWorkTable_projectId" name="mutualWorkTable_projectId" class = "without_dojo"
                    onchange="mutualWorkTable_reloadTable()">
                <option value="0" label="Все">Все</option>
            </select>
        </td>
    </tr>
    <tr>
        <td><label>Подразделение сотрудника</label></td>
        <td>
            <select data-dojo-id="mutualWorkTable_divisionEmployeeId" id="mutualWorkTable_divisionEmployeeId"
                    onchange="mutualWorkTable_reloadTable()">
                <option value="0" label="Все">Все</option>
                    <c:forEach items="${divisionList}" var="division">
                <option value="${division.id}" label="${division.name}">${division.name}</option>
                </c:forEach>
            </select>
        <td>
    </tr>
</table>

<button data-dojo-id="mutualWorkTable_addEmployeesButton" id="mutualWorkTable_addEmployeesButton"
        onclick="mutualWorkTable_addNewEmployees()">Добавить сотрудников</button>
<button onclick="mutualWorkTable_deleteRows()">Удалить выделенные строки</button>

<table data-dojo-id="mutualWorkTable" data-dojo-type="dojox.grid.DataGrid" height="500px">
    <thead>
    <tr>
        <th field="division_owner_name" width="180px">Центр-владелец</th>
        <th field="project_name" width="180px">Проект/Пресейл</th>
        <th field="project_type_name" width="75px">Тип</th>
        <th field="employee_name" width="150px">Сотрудник</th>
        <th field="division_employee_name" width="200px">Центр сотрудника</th>
        <th field="region_name" width="100px">Регион</th>
        <th field="work_days" width="50px" editable="true" formatter= "monthReport_colorCell">Рабочие дни</th>
        <th field="overtimes" width="50px" editable="true" formatter= "monthReport_colorCell">Переработки</th>
        <th field="coefficient" width="50px" editable="true" formatter= "monthReport_colorCell">Коэффициент</th>
        <th field="work_days" width="50px">Расч. раб. дни</th>
        <th field="overtimes" width="50px">Расч. переработки</th>
        <th field="image" width = "75px" formatter = "addImage">Детальная информация</th>
        <th field="comment" width="100px" editable="true">Комментарий</th>
    </tr>
    </thead>
</table>


<script type="text/javascript">

    var projectListWithOwnerDivision = ${projectListWithOwnerDivision};

    dojo.addOnLoad(function () {
        mutualWorkTable_createStore();
        mutualWorkTable_divisionChanged();

        var div = getCookieValue('aplanaDivision');
        div = div ? div : 0;
        dojo.byId("mutualWorkTable_divisionOwnerId").value = div;
        dojo.byId("mutualWorkTable_divisionEmployeeId").value = div;
        fillProjectListByDivision(dojo.byId("mutualWorkTable_divisionOwnerId").value, dojo.byId("mutualWorkTable_projectId"), null);

        monthReport_cellsValidator(mutualWorkTable, "comment");
    });

    function mutualWorkTable_addNewEmployees(){
        mutualWorkTable_employeeDialogShow();
        addEmployeesForm_returnEmployees = mutualWorkTable_returnEmployees;
    }

    function mutualWorkTable_employeeDialogShow(){
        // изменение значений на форме "Добавить сотрудника"
        dojo.byId("addEmployeesForm_divisionOwnerId").value = mutualWorkTable_divisionOwnerId.value;
        dojo.byId("addEmployeesForm_divisionId").value = mutualWorkTable_divisionEmployeeId.value;
        // значения изменились - необходимо запустить функции, обработчики изменений
        addEmployeesForm_updateLists();
        // Открыть форму добавления сотрудников
        addEmployeesForm_employeeDialog.show();
    }

    var addImage = function(value, rowIndex, cell) {
        // Если у сотрудника присутствует поле mutual_work_id (из таблицы mutual_work в базе данных) - показатель того,
        // что запись была добавлена вручную, то детальный отчет создать невозможно, поэтому делаем фон "лупы" непрозрачным, убираем кликабельность.
        // подробнее - APLANATS-1935
       if (mutualWorkTable.getItem(rowIndex).mutual_work_id[0] == null) {
           return "<a href='#' onclick='getReport3(" + rowIndex + ")'><img src='/resources/img/view.png' width='25' height='25'/></a>";
       } else {
           cell.customStyles.push('opacity: 0.2;');
           return "<img src='/resources/img/view.png' width='25' height='25'/>";
       }
    }

    function mutualWorkTable_divisionChanged() {
        fillProjectListByDivision(dojo.byId("mutualWorkTable_divisionOwnerId").value, dojo.byId("mutualWorkTable_projectId"), null);
    }

    function getStore() {
        var data = {
            identifier: 'identifier',
            items: []
        };
        return data;
    }

    function mutualWorkTable_createStore() {
        var data = {
            identifier: 'identifier',
            items: []
        };
        var store = new dojo.data.ItemFileWriteStore({data: data});
        mutualWorkTable.setStore(store);
    }

    function mutualWorkTable_reloadTable() {
        //дизактивируем кнопку "Сохранить"
        monthReport_saveButton.disabled = true;
        // меняем видимость кнопки "Добавить сотрудников"
        if (mutualWorkTable_divisionOwnerId.value == ALL_VALUE ||
                mutualWorkTable_divisionEmployeeId.value == ALL_VALUE) {
            mutualWorkTable_addEmployeesButton.disabled = true;
        } else {
            mutualWorkTable_addEmployeesButton.disabled = false;
        }
        if (mutualWorkTable.store.isDirty()) {

            if (!confirm("В таблице были изменения. Вы уверены, что хотите обновить данные не записав текущие?")) {
                return;
            }
        }
        var divisionOwner = dojo.byId("mutualWorkTable_divisionOwnerId").value;
        var divisionEmployee = dojo.byId("mutualWorkTable_divisionEmployeeId").value;
        var regions = "[" + getSelectValues(mutualWorkTable_regionListId) + "]";
        var year = dojo.byId("monthreport_year").value;
        var month = dojo.byId("monthreport_month").value;
        var projectId = dojo.byId("mutualWorkTable_projectId").value;

        mutualWorkTable_createStore();

        makeAjaxRequest(
                "<%= request.getContextPath()%>/monthreport/getMutualWorks",
                {
                    divisionOwner: divisionOwner,
                    divisionEmployee: divisionEmployee,
                    projectId: projectId,
                    regions: regions,
                    year: year,
                    month: month
                },
                "json",
                "Во время запроса данных для таблицы 'Взаимная занятость' произошла ошибка. Пожалуйста, свяжитесть с администраторами системы.",
                function (data) {
                    dojo.forEach(data, function (data) {
                        mutualWorkTable.store.newItem(data);
                    });
                    mutualWorkTable.store.save();
                    //делаем кнопку "Сохранить" активной
                    monthReport_saveButton.disabled = false;
                }
        );
    }

    function mutualWorkTable_addRows(mutualWork_list) {
        for (var i = 0; i < mutualWork_list.length; i++) {
            var newItem = mutualWork_list[i];
            mutualWorkTable.store.fetch({
                query: {identifier: newItem.identifier}, queryOptions: {deep: true},
                onComplete: function (items) {
                    if (items.length == 0) { // ранее не было, добавляем
                        mutualWorkTable.store.newItem(newItem);
                        return;
                    }
                }
            });
        }
    }

    function mutualWorkTable_deleteRows(){
        var items = mutualWorkTable.selection.getSelected();
        var jsonData = itemToJSON(mutualWorkTable.store, items);
        if (!confirm("Вы уверены, что хотите удалить выделенные строки?")) {
            return;
        }
            makeAjaxRequest(
                    "<%= request.getContextPath()%>/monthreport/deleteMutualWorks",
                    {
                        jsonData: "[" + jsonData + "]"
                    },
                    "text",
                    "Во время удаления данных из таблицы 'Взаимная занятость' произошла ошибка. Пожалуйста, свяжитесть с администраторами системы.",
                    function () {
                        if (items.length) {
                            dojo.forEach(items, function (selectedItem) {
                                if (selectedItem !== null) {
                                    mutualWorkTable.store.deleteItem(selectedItem);
                                }
                                mutualWorkTable.store.save();
                            });
                        }
                    }
            );
    }

    function mutualWorkTable_save() {
        mutualWorkTable.store.fetch({
            query: {}, queryOptions: {deep: true},
            onComplete: function (items) {
                mutualWorkTable.store.save();
                var jsonData = itemToJSON(mutualWorkTable.store, items);
                var divisionOwner = dojo.byId("mutualWorkTable_divisionOwnerId") ? mutualWorkTable_divisionOwnerId.value : 0;
                makeAjaxRequest(
                        "<%= request.getContextPath()%>/monthreport/saveMutualWorkTable",
                        {
                            year: dojo.byId("monthreport_year").value,
                            month: dojo.byId("monthreport_month").value,
                            divisionOwner: divisionOwner,
                            jsonData: "[" + jsonData + "]"
                        },
                        "text",
                        "Во время сохранения таблицы 'Взаимная занятость' произошла ошибка. Пожалуйста, свяжитесть с администраторами системы.",
                        function () {
                            mutualWorkTable_reloadTable();
                        }
                );
            }
        });
    }

    function getReport3(rowIndex) {
        processing();
        var item = mutualWorkTable.getItem(rowIndex);
        var divisionOwner = parseInt(item.division_owner_id);
        var divisionEmployee = parseInt(item.division_employee_id);
        var region = parseInt(item.region_id);
        var employeeId = parseInt(item.employee_id);
        var year = dojo.byId("monthreport_year").value;
        var month = dojo.byId("monthreport_month").value;

        var projectId = parseInt(item.project_id);
        var beginDate = year + "-" + month + "-" + getFirstDayOfMonth(year, month);
        var endDate = year + "-" + month + "-" + getLastDayOfMonth(year, month);
        window.location = "<%= request.getContextPath()%>/monthreport/prepareReport3Data/" + divisionOwner + "/" +
                          divisionEmployee + "/" + region + "/" + employeeId + "/" + projectId + "/" + beginDate + "/" +
                          endDate;
        stopProcessing();
    }

    // обрабатывает кнопку "Добавить" на форме "Добавить сотрудника"
   var mutualWorkTable_returnEmployees = function(){
        var typeSelect            = dojo.byId("addEmployeesForm_projectTypeId");
        var typeId                = parseInt(typeSelect.value);
        var type                  = typeSelect.options[typeSelect.selectedIndex].text;
        var projectSelect         = dojo.byId("addEmployeesForm_projectId");
        var projectId             = projectSelect.value;
        var divisionOwnerSelect   = dojo.byId("addEmployeesForm_divisionOwnerId");
        var divisionOwnerId       = parseInt(divisionOwnerSelect.value);
        var divisionOwnerName     = divisionOwnerSelect.options[divisionOwnerSelect.selectedIndex].text;
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
                mutual_work_id:        0,   // присваиваем данному полю значение 0 для того, чтобы "затенять" лупу у только что добавленной записи.
                employee_id: parseInt(employee.value),
                employee_name:   employee.innerHTML,
                division_employee_id: parseInt(employee.attributes.div_id.value),
                division_employee_name:   employee.attributes.div_name.value,
                division_owner_id: divisionOwnerId,
                division_owner_name:   divisionOwnerName,
                region_id:   parseInt(employee.attributes.reg_id.value),
                region_name:     employee.attributes.reg_name.value,
                project_type_name:   type,
                project_type_id:   typeId,
                project_id:  projectId,
                project_name:    project,
                work_days: 0.0,
                overtimes:   0.0,
                coefficient:    0.0,
                work_days_calculated: 0.0,
                overtimes_calculated: 0.0,
                comment:    ""
            });
        });
        mutualWorkTable_addRows(employee_list);
    }

</script>