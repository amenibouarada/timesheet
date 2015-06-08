<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<table class="dijitDialogPaneContentArea no_border employmentPlanningTable">
    <tr>
        <td><label>Подразделение владельца</label></td>
        <td>
            <select id="overtimeTable_divisionOwnerId">
                <c:forEach items="${divisionList}" var="division">
                <option value="${division.id}" label="${division.name}">
                    </c:forEach>
            </select>
        <td>
    </tr>
    <tr>
        <td><label>Подразделение сотрудника</label></td>
        <td>
            <select id="overtimeTable_divisionEmployeeId">
                <c:forEach items="${divisionList}" var="division">
                <option value="${division.id}" label="${division.name}">
                    </c:forEach>
            </select>
        <td>
    </tr>
</table>

<button onclick="reloadTable()">Показать</button>

<button onclick="saveOvertimeTable()">Сохранить</button>
<button onclick="employeeDialog.show()">Добавить сотрудников</button>
<button onclick="deleteRows()">Удалить выделенные строки</button>

<table data-dojo-id="overtimeTable" data-dojo-type="dojox.grid.DataGrid" style="height: 500px">
    <thead>
        <tr>
            <th field="id" width="20px">
                <%--<img src="<c:url value='/resources/img/add.gif'/>" class="create_img" title="Создать"--%>
                     <%--onclick="employeeDialog.show();"/>--%>
            </th>
            <th field="employee"                width="150px"   >Сотрудник</th>
            <th field="division"                width="150px"   >Подразделение</th>
            <th field="region"                  width="100px"   >Регион</th>
            <th field="type"                    width="150px"   >Тип</th>
            <th field="project"                 width="100px"   >Проект/Пресейл</th>
            <th field="overtime"                width="100px"   editable="true" >Переработки</th>
            <th field="premium"                 width="100px"   editable="true" >Премия</th>
            <th field="allAccountedOvertime"    width="110px"   editable="true" >Всего учтенных переработок и премий</th>
            <th field="comment"                 width="200px"   editable="true" >Комментарий</th>
        </tr>
    </thead>
</table>

<script type="text/javascript">

    dojo.addOnLoad(function(){
        var overtimeList = "${overtimeList}";
        if (overtimeList){
            addRows(overtimeList);
        }

        var div = getCookieValue('aplanaDivision');
        div = div ? div : 0;
        dojo.byId("overtimeTable_divisionOwnerId").value = div;
        dojo.byId("overtimeTable_divisionEmployeeId").value = div;

        createStore();
    });

    function createStore(){
        var data = {
            identifier: 'employee',
            items: []
        };
        var store = new dojo.data.ItemFileWriteStore({data: data});
        overtimeTable.setStore(store);
    }

    function reloadTable(){
        var divisionOwner = dojo.byId("overtimeTable_divisionOwnerId").value;
        var divisionEmployee = dojo.byId("overtimeTable_divisionEmployeeId").value;
        var year = dojo.byId("monthreport_year").value;
        var month = dojo.byId("monthreport_month").value;
        createStore();

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
                addRows(dojo.fromJson(response));
                //alert(response);
            },
            error: function (response, ioArgs) {
                //alert('Error saving Overtime table!');
            }
        });
    }

    function addRows(overtime_list){
        for(var i=0; i < overtime_list.length; i++){
            try{
                overtimeTable.store.newItem(overtime_list[i]);
            }catch(exc){
                // ToDo сделать нормальную проверку, на то, что сотрудник уже добавлен
            }

        }
    }

    function deleteRows(){
        var items = overtimeTable.selection.getSelected();

        dojo.xhrPost({
            url: "monthreport/deleteOvertimes",
            content: {
                jsonData: "[" + itemToJSON(overtimeTable.store, items) + "]"
            },
            handleAs: "text",
            load: function (response, ioArgs) {
                //alert(response);
            },
            error: function (response, ioArgs) {
                //alert('Error saving Overtime table!');
            }
        });


        if(items.length){
            dojo.forEach(items, function(selectedItem){
                if(selectedItem !== null){
                    overtimeTable.store.deleteItem(selectedItem);
                }
            });
        }
    }

    function saveOvertimeTable(){

//                                    isChanged: 1
        overtimeTable.store.fetch({query: {}, queryOptions: {deep: true},
            onComplete: function (items) {
                            postAjaxToSave(itemToJSON(overtimeTable.store, items))
                        }
        });

        function postAjaxToSave(jsonData){
            dojo.xhrPost({
                url: "monthreport/saveOvertimeTable",
                content: {
                    year: dojo.byId("monthreport_year").value,
                    month: dojo.byId("monthreport_month").value,
                    jsonData: "[" + jsonData + "]"
                },
                handleAs: "text",
                load: function (response, ioArgs) {
                    //alert(response);
                },
                error: function (response, ioArgs) {
                    //alert('Error saving Overtime table!');
                }
            });
        }
    }

    // Переводит объект в JSON
    function itemToJSON(store, items) {
        var data = [];
        if (items && store) {
            for (var n = 0; n < items.length; ++n) {
                var json = {};
                var item = items[n];
                if (item) {
                    // Determine the attributes we need to process.
                    var attributes = store.getAttributes(item);
                    if (attributes && attributes.length > 0) {
                        for (var i = 0; i < attributes.length; i++) {
                            var values = store.getValues(item, attributes[i]);
                            if (values) {
                                // Handle multivalued and single-valued attributes.
                                if (values.length > 1) {
                                    json[attributes[i]] = [];
                                    for (var j = 0; j < values.length; j++) {
                                        var value = values[j];
                                        // Check that the value isn't another item. If it is, process it as an item.
                                        if (store.isItem(value)) {
                                            json[attributes[i]].push(dojo.fromJson(itemToJSON(store, value)));
                                        } else {
                                            json[attributes[i]].push(value);
                                        }
                                    }
                                } else {
                                    if (store.isItem(values[0])) {
                                        json[attributes[i]] = dojo.fromJson(itemToJSON(store, values[0]));
                                    } else {
                                        json[attributes[i]] = values[0];
                                    }
                                }
                            }
                        }
                    }
                    data.push(dojo.toJson(json));
                }
            }
        }
        return data;
    }
</script>