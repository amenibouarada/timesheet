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



<table data-dojo-id="mutualWorkTable" data-dojo-type="dojox.grid.DataGrid" height="500px">
    <thead>
    <tr>
        <th field="divisionOwnerName" width="200px">Центр-владелец</th>
        <th field="projectName" width="200px">Проект/Пресейл</th>
        <th field="projectTypeName" width="100px">Тип</th>
        <th field="employeeName" width="150px">Сотрудник</th>
        <th field="divisionEmployeeName" width="200px">Центр сотрудника</th>
        <th field="regionName" width="100px">Регион</th>
        <th field="workDays" width="100px" editable="true">Рабочие дни</th>
        <th field="overtimes" width="100px" editable="true">Переработки</th>
        <th field="coefficient" width="100px" editable="true">Коэффициент</th>
        <th field="workDaysCalc" width="100px">Расч. раб. дни</th>
        <th field="overtimesCalc" width="100px">Расч. переработки</th>
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

        var prevValue;
        var fieldName;
        mutualWorkTable.onStartEdit = function (inCell, inRowIndex) {
            fieldName = inCell.field;
            prevValue = mutualWorkTable.store.getValue(mutualWorkTable.getItem(inRowIndex), fieldName);
        }

        mutualWorkTable.onApplyCellEdit = function (inValue, inRowIndex, inFieldIndex) {
            if (isNaN(Number(inValue))) {
                mutualWorkTable.store.setValue(mutualWorkTable.getItem(inRowIndex), fieldName, prevValue);
            }
        }
    });

    var addImage = function(value, rowIndex, cell) {
        return "<a href='#' onclick='getReport3("+rowIndex+")'><img src='/resources/img/view.png' width='25' height='25'/></a>";
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

        processing();
        dojo.xhrPost({
            url: "monthreport/getMutualWorks",
            content: {
                divisionOwner: divisionOwner,
                divisionEmployee: divisionEmployee,
                projectId: projectId,
                regions: regions,
                year: year,
                month: month
            },
            handleAs: "text",
            load: function (response, ioArgs) {
                stopProcessing();
                var mutualWorks = dojo.fromJson(response);
                dojo.forEach(mutualWorks, function (mutualWork) {
                    mutualWorkTable.store.newItem(mutualWork);
                });
                mutualWorkTable.store.save();
            },
            error: function (response, ioArgs) {
                stopProcessing();
                alert(response);
            }
        });
    }

    function mutualWorkTable_save() {
        processing();
        mutualWorkTable.store.fetch({
            query: {}, queryOptions: {deep: true},
            onComplete: function (items) {
                mutualWorkTable.store.save();
                var jsonData = itemToJSON(mutualWorkTable.store, items);
                dojo.xhrPost({
                    url: "monthreport/saveMutualWorkTable",
                    content: {
                        year: dojo.byId("monthreport_year").value,
                        month: dojo.byId("monthreport_month").value,
                        jsonData: "[" + jsonData + "]"
                    },
                    handleAs: "text",
                    load: function (response, ioArgs) {
                        stopProcessing();
                        alert(response);
                        mutualWorkTable_reloadTable();
                    },
                    error: function (response, ioArgs) {
                        stopProcessing();
                        alert(response);
                    }
                });
            }
        });
    }

    function getReport3(rowIndex) {
        processing();
        var item = mutualWorkTable.getItem(rowIndex);
        var divisionOwner = parseInt(item.divisionOwnerId);
        var divisionEmployee = parseInt(item.divisionEmployeeId);
        var region = parseInt(item.regionId);
        var employeeId = parseInt(item.employeeId);
        var year = dojo.byId("monthreport_year").value;
        var month = dojo.byId("monthreport_month").value;

        var projectId = parseInt(item.projectId);
        var beginDate = year + "-" + month + "-" + getFirstDayOfMonth(year, month);
        var endDate = year + "-" + month + "-" + getLastDayOfMonth(year, month);

        window.location = "<%= request.getContextPath()%>/monthreport/prepareReport3Data/" + divisionOwner + "/" +
                          divisionEmployee + "/" + region + "/" + employeeId + "/" + projectId + "/" + beginDate + "/" +
                          endDate;
        stopProcessing();
    }

</script>