<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@ page import="static com.aplana.timesheet.system.constants.TimeSheetConstants.DOJO_PATH" %>
<%@ page import="static com.aplana.timesheet.enums.MonthReportStatusEnum.*" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>

<html>
<head>
    <title>Табель</title>

    <style type="text/css">
        @import "<%= DOJO_PATH %>/dojox/grid/resources/tundraGrid.css";
        @import "<%= getResRealPath("/resources/css/DataGrid.ext.css", application) %>";
        @import "<%= getResRealPath("/resources/css/monthreport.css", application) %>";
    </style>

    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/monthreport.js"></script>
    <script src="<%= getResRealPath("/resources/js/DataGrid.ext.js", application) %>" type="text/javascript"></script>

    <script type="text/javascript">
        dojo.require("dojo.parser");
        dojo.require("dojox.grid.DataGrid");
        dojo.require("dojo.data.ItemFileWriteStore");
        dojo.require("dijit.layout.TabContainer");
        dojo.require("dijit.layout.ContentPane");

        var statusList = {
            open:       {id: <%=OPEN.getId()%>,         name: '<%=OPEN.getName()%>'},
            closed:     {id: <%=CLOSED.getId()%>,       name: '<%=CLOSED.getName()%>'},
            inWork:     {id: <%=IN_WORK.getId()%>,      name: '<%=IN_WORK.getName()%>'},
            notCreated: {id: <%=NOT_CREATED.getId()%>,  name: '<%=NOT_CREATED.getName()%>'}
        };

        // Номера редактируемых полей в таблицах
        var monthReportTable_editableColumns = [7, 9, 13, 16, 17];
        var overtimeTable_editableColumns = [5, 6, 8, 9];
        var mutualWorkTable_editableColumns = [6, 7, 8, 12];

        function monthreport_getStatusById(/* int */ id){
            if (statusList.open.id == id){ return statusList.open; }
            if (statusList.closed.id == id){ return statusList.closed; }
            if (statusList.inWork.id == id){ return statusList.inWork; }
            if (statusList.notCreated.id == id){ return statusList.notCreated; }
        }

        var projectListWithOwnerDivision = ${projectListWithOwnerDivision};
        var managerMapJson = ${managerList};

        //Массив всплывающих подсказок для таблицы "Табель"
        var monthReportTable_tooltips = ["Сотрудник", "Подразделение", "Регион", "", "", "", "", "Отработано", "Оплаченные переработки этого месяца",
            "Переработки, не отгуленные за прошедшие периоды на начало месяца", "Отпуск с сохранением фактический",
            "Отпуск без сохранения фактический", "Переработки, отгуленные в этом месяце", "Количество рабочих дней болезни за месяц",
            "Всего оплаченных рабочих дней", "Всего оплаченных переработок", "Накопленный отпуск на конец месяца за вычетом отпуска за свой счет, отгуленного в этом месяце ",
            "Переработки, не оплаченные и не отгуленные за прошедшие периоды на начало месяца плюс переработки не отгуленные ",
            "Количество часов, списанных во время отпуска с сохранением содержания или отгула", "Отпуск, начисленный в этом месяце",
            "Переработки, начисленные в отгул в этом месяце", "Количество рабочих дней болезни итого",
            "Количество рабочих дней болезни с больничным", "Количество рабочих дней болезни без подтверждения",
            "Количество часов, списанных во время болезни", "Отработано по плану", "Отработано фактически"
        ];

       //Массив всплывающих подсказок для таблицы "Переработки"
        var overtimeTable_tooltips = ["Сотрудник", "Подразделение", "Регион", "Проект/Пресейл", "Тип", "Переработки, отработанные дни",
            "Переработки, дополнительные дни", "Всего переработок", "Из них финансово компенсируемые", "Комментарий"
        ];

        //Массив всплывающих подсказок для таблицы "Взаимная занятость"
        var mutualWorkTable_tooltips = ["Центр-владелец", "Проект/Пресейл", "Тип", "Сотрудник", "Центр сотрудника", "Регион",
            "Рабочие дни", "Переработки", "Коэффициент", "Расчетные рабочие дни", "Расчетные переработки",
            "Детальная информация", "Комментарий"
        ];

        /**
         * Функция для выполнения ajax-запросов
         * Получает стандартные параметры, для запросов, отличается логикой обработки запросов
         * Если ожидается текстовый ответ, то он выводится пользователю, если же после вывода этого сообщения необходимо
         * выполнить какие-то действие, то будет вызвана функция handler без параметров.
         * Если ожидается ответ в виде json-объекта, то будет попытка преобразовать полученное сообщение в json-объект,
         * в случае успеха, будет запущена функция handler с единственным параметром - полученным и преобразованном json-параметром
         * Если ожидается ответ в виде json-объекта, но преобразование не удалось, то в этом случае полученный ответ
         * будет выведен как текстовое сообщение пользователю
         * Если во время выполнения запроса произошла ошибка, то пользователю отобразится текст указанный в параметре errorMessage
         *
         * @param url - адрес на который будет отправлен запрос
         * @param responseType - тип, который ожидается получить
         * @param content - параметры запроса
         * @param errorMessage - сообщение, которое отобразится пользователю при ошибке
         * @param handler - обработчик полученного сообщения
         * @param doProcessing - признак того, использовать ли "крутилку"
        */
        function makeAjaxRequest(url, content, responseType, errorMessage, handler, doProcessing){
            doProcessing = doProcessing != null ? doProcessing : true; // значение по умолчанию
            if (doProcessing == true) {
                processing();
            }
            dojo.xhrPost({
                url:        url,
                handleAs:   "text",
                content:    content,
                preventCache: false,
                load: function (response, ioargs) {
                    if (responseType == "text" && response != ''){
                        alert(response);
                        handler();
                    }else if (responseType == "json"){
                        try{
                            handler(dojo.fromJson(response));
                        }catch(exc){
                            console.log(exc);
                            alert(response);
                        }
                    }
                    if (doProcessing == true) {
                        stopProcessing();
                    }
                },
                error: function() {
                    if (doProcessing == true) {
                        stopProcessing();
                    }
                    alert(errorMessage);
                }
            });
        }

        // Заполняет таблицы во время ajax-запроса
        function fillStore(currentTable, data) {
            dojo.forEach(data, function (data) {
                currentTable.store.newItem(data);
            });
            currentTable.store.save();
            //делаем кнопку "Сохранить" активной
            monthReport_saveButtonChangeState(true);
            stopProcessing();
        }

        // раскраска ячеек и проверка на существующее значение заполненности таблицы реальными данными, а не автовычисленными
        // и добавляю подсказку
        function monthReport_colorCell(value, rowIndex, cell) {
            var item = this.grid.getItem(rowIndex);
            var calculatedValue = this.grid.store.getValue(item, cell.field + "_calculated", null);
            var dispValue = "";
            if (value && value != "null" || value == "0"){
                cell.customStyles.push('color:green');
                dispValue = value != null ? value : '';
            }else{
                cell.customStyles.push('color:red');
                dispValue = calculatedValue != null ? calculatedValue : '';
            }
            // Если cell.name == "<div>&nbsp;</div>" - это показатель того, что колонка свёрнута. Чтобы в этом случае не отображались символы,
            // возвращаем пустое значение.
            return cell.name == "<div>&nbsp;</div>" ? "" : "<span title='Значение по умолчанию: " + calculatedValue + "'>" + dispValue + "</span>";
        }

        // Создаёт всплывающие подсказки для заголовков таблиц
        function createTooltips(tooltips, grid) {
            var showTooltip = function (e) {
                var msg = tooltips[e.cell.index];
                if (msg) {
                    dijit.showTooltip(msg, e.cellNode);
                }
            };
            var hideTooltip = function (e) {
                dijit.hideTooltip(e.cellNode);
            };
            dojo.connect(grid, "onHeaderCellMouseOver", showTooltip);
            dojo.connect(grid, "onHeaderCellMouseOut", hideTooltip);
        }

        /**
         * Раскрашивает месяцы в выпадашке в зависимости от статуса, делает месяцы и годы доступными/недоступными
         * Краткое описание алгоритма:
         * Получает статусы всех табелей. Если нет ни одного созданного табеля, то делает доступным для просмотра январь текущего года,
         * все остальные месяцы и годы делает недоступными. Если есть созданные табели, то проходит все эти табели по циклу. Если табель открыт,
         * то делает все табели за все следующие месяцы недоступными для просмотра. Если попадается закрытый табель, то делаем следующий месяц
         * доступным для просмотра. Если это закрытый табель за декабрь (следующий месяц - январь), то делаем доступным для просмотра следующий год
         * и январь следующего года.
         */
        function monthReport_colorizeMonthOption(){
            makeAjaxRequest(
                    "<%= request.getContextPath()%>/monthreport/getMonthReportStatusesForYear",
                    { year: monthreport_year.value },
                    "json",
                    "При запросе статусов табелей за год произошла ошибка",
                    function (response) {
                        // Временно, до лучших времён задизаблил январь и февраль. До той поры, пока
                        // не появится постоянное решение по этому поводу
                        dojo.byId("monthreport_month_option_1").disabled = true;
                        dojo.byId("monthreport_month_option_2").disabled = true;
                        // сперва очистим, потом расскрасим
                        for (var i = 1; i <= 12; i++){
                            dojo.byId("monthreport_month_option_" + i).style.backgroundColor = "white";
                        }
                        if (response == '') {
                            var nextMonth = 2;
                            var nextYear = dojo.byId("monthreport_year").selectedIndex + 1;
                            var lastYear = dojo.byId("monthreport_year").length;
                            monthReport_monthsYearsDisable(nextMonth, nextYear, lastYear);
                        } else {
                            response.forEach(function (item) {
                                var month = item[0];
                                var nextMonth = month < 12 ? month + 1 : 1;
                                var status = item[1];
                                var closeDate = item[2];
                                var option = dojo.byId("monthreport_month_option_" + month);
                                var nextYear = dojo.byId("monthreport_year").selectedIndex + 1;
                                var lastYear = dojo.byId("monthreport_year").length;
                                switch (status) {
                                    case statusList.closed.id:
                                        option.style.backgroundColor = "green";
                                        dojo.byId("monthreport_month_option_" + nextMonth).disabled = false;
                                        if (nextMonth == 1) {
                                            dojo.byId("monthreport_year_option_" + nextYear).disabled = false;
                                        }
                                        monthReport_monthsYearsDisable(nextMonth + 1, nextYear, lastYear);
                                        break;
                                    case statusList.open.id:
                                        option.style.backgroundColor = "red";
                                        if (closeDate) {
                                            dojo.byId("monthreport_month_option_" + nextMonth).disabled = false;
                                        } else {
                                            monthReport_monthsYearsDisable(nextMonth, nextYear, lastYear);
                                        }
                                        break;
                                }
                            });
                        }
                    },
                    false
            );
        }

        function monthReport_monthsYearsDisable(nextMonth, nextYear, lastYear) {
            for (var i = nextMonth ; i <= 12; i++) {
                dojo.byId("monthreport_month_option_" + i).disabled = true;
            }
            for (var i = nextYear; i <= lastYear; i++ ) {
                dojo.byId("monthreport_year_option_" + i).disabled = true;
            }
        }

        <sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
        function makeReport(tabNum) {
            processing();
            var year = dojo.byId("monthreport_year").value;
            var month = dojo.byId("monthreport_month").value;

            switch (tabNum) {
                case 1:
                    var division = monthReportTable_divisionId.value;
                    var manager = monthReportTable_managerId.value;
                    var regions = "[" + getSelectValues(monthReportTable_regionListId) + "]";
                    var roles = "[" + getSelectValues(monthReportTable_projectRoleListId) + "]";
                    var year = year;
                    var month = month;
                    window.location = "<%= request.getContextPath()%>/monthreport/makeMonthReport/" + division + "/" +
                                       manager + "/" + regions + "/" + roles + "/" + year + "/" + month;
                    break;
                case 2:
                    var divisionOwner = dojo.byId("overtimeTable_divisionOwnerId").value;
                    var divisionEmployee = dojo.byId("overtimeTable_divisionEmployeeId").value;
                    window.location = "<%= request.getContextPath()%>/monthreport/makeOvertimeReport/" + year + "/" +
                                      month + "/" + divisionOwner + "/" + divisionEmployee;
                    break;
                case 3:
                    var divisionOwner = dojo.byId("mutualWorkTable_divisionOwnerId").value;
                    var divisionEmployee = dojo.byId("mutualWorkTable_divisionEmployeeId").value;
                    var projectId = dojo.byId("mutualWorkTable_projectId").value;
                    var regions = "[" + getSelectValues(mutualWorkTable_regionListId) + "]";
                    window.location = "<%= request.getContextPath()%>/monthreport/makeMutualWorkReport/" + year + "/" +
                                      month + "/" + regions + "/" + divisionOwner + "/" + divisionEmployee + "/" + projectId;
                    break;
            }
            stopProcessing();
        }
        </sec:authorize>

        var eventConnections = [];

        dojo.addOnLoad(function () {
            // установим год и месяц по умолчанию
            var currentDate = new Date();
            dojo.byId("monthreport_year").value = ${lastEnableYearAndMonth}[0][0] || currentDate.getFullYear();
            dojo.byId("monthreport_month").value = ${lastEnableYearAndMonth}[0][1] + 3 || 3;

            //Инициализируем вложенные таблицы
            monthReport_initMonthReportTable();
            monthReport_initOvertimeTable();
            monthReport_initMutualWorkTable();

            // функция для переназначения обработчиков нажатия кнопок
            // и отображения актуальных данных
            var changeButtonListeners = function () {
                dojo.forEach(eventConnections, dojo.disconnect);
                eventConnections = [];
                if (dijit.byId('tabContainer').selectedChildWidget.id == "monthReportTable_tab") {
                    monthReportTable_reloadTable();
                    eventConnections.push(dojo.connect(monthreport_year, "onchange", function () {
                        monthReportTable_reloadTable();
                    }));
                    eventConnections.push(dojo.connect(monthreport_month, "onchange", function () {
                        monthReportTable_reloadTable();
                    }));
                    <sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
                    eventConnections.push(dojo.connect(monthReport_saveButton, "onclick", function () {
                        monthReportTable_save()
                    }));
                    eventConnections.push(dojo.connect(monthReport_exportButton, "onclick", function () {
                        makeReport(1)
                    }));
                    </sec:authorize>
                }
                if (dijit.byId('tabContainer').selectedChildWidget.id == "overtimeTable_tab") {
                    overtimeTable_reloadTable();
                    eventConnections.push(dojo.connect(monthreport_year, "onchange", function () {
                        overtimeTable_reloadTable();
                    }));
                    eventConnections.push(dojo.connect(monthreport_month, "onchange", function () {
                        overtimeTable_reloadTable();
                    }));
                    <sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
                    eventConnections.push(dojo.connect(monthReport_saveButton, "onclick", function () {
                        overtimeTable_save();
                    }));
                    eventConnections.push(dojo.connect(monthReport_exportButton, "onclick", function () {
                        makeReport(2)
                    }));
                    </sec:authorize>
                }
                <sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
                if (dijit.byId('tabContainer').selectedChildWidget.id == "mutualWorkTable_tab") {
                    mutualWorkTable_reloadTable();
                    eventConnections.push(dojo.connect(monthreport_year, "onchange", function () {
                        mutualWorkTable_reloadTable();
                    }));
                    eventConnections.push(dojo.connect(monthreport_month, "onchange", function () {
                        mutualWorkTable_reloadTable();
                    }));
                    eventConnections.push(dojo.connect(monthReport_saveButton, "onclick", function () {
                        mutualWorkTable_save();
                    }));
                    eventConnections.push(dojo.connect(monthReport_exportButton, "onclick", function () {
                        makeReport(3);
                    }));
                }
                </sec:authorize>
            }
            // назначим слушителей переключения табов
            tabContainer.watch("selectedChildWidget", changeButtonListeners);
            changeButtonListeners(); // выполним, чтобы загрузить слушателей для первой вкладки
            monthReport_colorizeMonthOption();
        });

        var divFromCookie = getCookieValue('aplanaDivision');
        divFromCookie = divFromCookie ? divFromCookie : 0;

        //Инициализация таблицы "Табель"
        function monthReport_initMonthReportTable() {
            monthReport_updateStatus();
            monthReportTable_createStore();
            if (dojo.byId("monthReportTable_divisionId")){
                monthReportTable_updateManagers();
                monthReportTable_divisionId.value = divFromCookie;
            }
            monthReport_setEditable(monthReportTable, monthReportTable_editableColumns, true);
            monthReport_cellsValidator(monthReportTable);
            createTooltips(monthReportTable_tooltips, monthReportTable);
        }

        //Инициализация таблицы "Переработки"
        function monthReport_initOvertimeTable() {
            overtimeTable_createStore();
            if (dojo.byId("overtimeTable_divisionOwnerId")){
                dojo.byId("overtimeTable_divisionOwnerId").value = divFromCookie;
                dojo.byId("overtimeTable_divisionEmployeeId").value = divFromCookie;
                monthReport_addEmployeesButtonsVisibleChange(overtimeTable_divisionOwnerId, overtimeTable_divisionEmployeeId, overtimeTable_addEmployeesButton);
            }
            monthReport_setEditable(overtimeTable, overtimeTable_editableColumns, true);
            monthReport_cellsValidator(overtimeTable, "comment");
            createTooltips(overtimeTable_tooltips, overtimeTable);
        }

        //Инициализация таблицы "Взаиммная занятость"
        function monthReport_initMutualWorkTable() {
            <sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
            mutualWorkTable_createStore();
            mutualWorkTable_divisionChanged();
            dojo.byId("mutualWorkTable_divisionOwnerId").value = divFromCookie;
            dojo.byId("mutualWorkTable_divisionEmployeeId").value = divFromCookie;
            fillProjectListByDivision(dojo.byId("mutualWorkTable_divisionOwnerId").value, dojo.byId("mutualWorkTable_projectId"), null);
            monthReport_cellsValidator(mutualWorkTable, "comment");
            createTooltips(mutualWorkTable_tooltips, mutualWorkTable);
            </sec:authorize>
        }

        //Функция для валидации введённых пользователем значений
        function monthReport_cellsValidator(currentTable, allowStringField) {
            var prevValue;
            var fieldName;
            currentTable.onStartEdit = function (inCell, inRowIndex) {
                fieldName = inCell.field;
                prevValue = currentTable.store.getValue(currentTable.getItem(inRowIndex), fieldName);
            }

            currentTable.onApplyCellEdit = function (inValue, inRowIndex, inFieldIndex) {
                // Проверяем, является ли для данного поля допустимым строковое значение
                if (inFieldIndex != allowStringField) {
                    if (isNaN(Number(inValue))) {
                        currentTable.store.setValue(currentTable.getItem(inRowIndex), fieldName, prevValue);
                    }
                }else if (inValue == "null") {currentTable.store.setValue(currentTable.getItem(inRowIndex), fieldName, "");}
            }
        }

        function monthReport_setEditable(currentTable, columns, editable) {
            <sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
            for (var i = 0; i < columns.length; i++) {
                var col = columns[i];
                if (currentTable.layout.cells[col]) {
                    currentTable.layout.cells[col].editable = editable;
                }
            }
            </sec:authorize>
        }

        function monthReport_updateStatus(){
            makeAjaxRequest(
                    "<%= request.getContextPath()%>/monthreport/getStatus",
                    {year: monthreport_year.value, month: monthreport_month.value},
                    "json",
                    "Не удалось получить статус",
                    function (status) {
                        monthReportStatus.innerHTML = status !== "" ? monthreport_getStatusById(status).name : "не удалось получить статус";
                        var editable = true;
                        var closeButton = dojo.byId("monthReport_closeButton");
                        var openButton = dojo.byId("monthReport_openButton");
                        var saveButton = dojo.byId("monthReport_saveButton");
                        if (status == statusList.closed.id){
                            if (closeButton){ // кнопка может быть не видна для некоторых ролей
                                closeButton.style.visibility = "hidden";
                                openButton.style.visibility = "visible";
                            }
                            if (saveButton) {
                                saveButton.style.visibility = "hidden";
                            }
                            editable = false;
                            monthReport_changeButtonsState(true);
                        }else if(status == statusList.notCreated.id){
                            if (closeButton){ // кнопка может быть не видна для некоторых ролей
                                closeButton.style.visibility = "hidden";
                                openButton.style.visibility = "hidden";
                            }
                            if (saveButton) {
                                saveButton.style.visibility = "visible";
                            }
                        }else{
                            if (closeButton){ // кнопка может быть не видна для некоторых ролей
                                closeButton.style.visibility = "visible";
                                openButton.style.visibility = "hidden";
                            }
                            if (saveButton) {
                                saveButton.style.visibility = "visible";
                            }
                            monthReport_changeButtonsState(false);
                        }
                        monthReport_setEditable(monthReportTable, monthReportTable_editableColumns, editable);
                        monthReport_setEditable(overtimeTable, overtimeTable_editableColumns, editable);
                        <sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
                        monthReport_setEditable(mutualWorkTable, mutualWorkTable_editableColumns, editable);
                        </sec:authorize>
                        monthReport_colorizeMonthOption();
                    },
                    false
            )
        }

        //Функция для изменения состояния кнопки "Сохранить". Необходима для того, чтобы невозможно было инициировать сохранение
        // до завершения формирования таблицы.
        // Параметр requredState - требуемое состояние кнопки (true - сделать активной, false - сделать неактивной)
        function monthReport_saveButtonChangeState(requiredState) {
            var saveButton = dojo.byId("monthReport_saveButton");
            if (saveButton) {
                if (requiredState == true) {
                    saveButton.disabled = false;
                }
                if (requiredState == false) {
                    saveButton.disabled = true;
                }
            }
        }

        //Функция для изменения состояния кнопок добавления и удаления строк. Необходима для отключения возможностей редактирования
        // в случае закрытия табеля.
        // Параметр disabled - требуемое состояние кнопки (true - сделать неактивной, false - сделать активной)
        function monthReport_changeButtonsState(disabled) {
            if (dojo.byId("overtimeTable_addEmployeesButton") && dojo.byId("overtimeTable_deleteRowsButton")) {
                dojo.byId("overtimeTable_addEmployeesButton").disabled = disabled;
                dojo.byId("overtimeTable_deleteRowsButton").disabled = disabled;
            }
            if (dojo.byId("mutualWorkTable_addEmployeesButton") && dojo.byId("mutualWorkTable_deleteRowsButton")) {
                dojo.byId("mutualWorkTable_addEmployeesButton").disabled = disabled;
                dojo.byId("mutualWorkTable_deleteRowsButton").disabled = disabled;
            }
        }

        function monthReport_addEmployeesButtonsVisibleChange(divisionOwnerId, divisionEmployeeId, button) {
            if (divisionOwnerId.value == ALL_VALUE ||
                    divisionEmployeeId.value == ALL_VALUE) {
                button.disabled = true;
            } else {
                button.disabled = false;
            }
        }

        // Вызывает форму добавления сотрудников
        function monthReport_employeeDialogShow(divisionOwnerId, divisionEmployeeId){
            // изменение значений на форме "Добавить сотрудника"
            dojo.byId("addEmployeesForm_divisionOwnerId").value = divisionOwnerId;
            dojo.byId("addEmployeesForm_divisionId").value = divisionEmployeeId;
            dojo.byId("addEmployeesForm_year").value = monthreport_year.value;
            dojo.byId("addEmployeesForm_month").value = monthreport_month.value;
            dojo.byId("addEmployeesForm_year").disabled = true;
            dojo.byId("addEmployeesForm_month").disabled = true;
            // значения изменились - необходимо запустить функции, обработчики изменений
            addEmployeesForm_updateLists();
            // Открыть форму добавления сотрудников
            addEmployeesForm_employeeDialog.show();
        }

        <sec:authorize access="hasRole('ROLE_MONTH_REPORT_MANAGER')">
        function monthReport_changeStatus(url){
            makeAjaxRequest(
                    url,
                    { year: monthreport_year.value, month: monthreport_month.value },
                    "text",
                    "Не удалось изменить статус табеля",
                    function (){
                        monthReport_updateStatus();
                    },
                    true
            );
        }
        function monthReport_close(){
            monthReport_changeStatus("<%= request.getContextPath()%>/monthreport/closeMonthReport");
        }
        function monthReport_open(){
            monthReport_changeStatus("<%= request.getContextPath()%>/monthreport/openMonthReport");
        }
        </sec:authorize>
    </script>

</head>
<body>
<h1>Табель</h1>

<table>
    <tr>
        <td>
            <span class="label">Год:</span>
        </td>
        <td>
            <select data-dojo-id="monthreport_year" id="monthreport_year" onchange="monthReport_updateStatus();">
                <option id="monthreport_year_option_1" value="2015" label="2015">2015</option>
                <option id="monthreport_year_option_2" value="2016" label="2016">2016</option>
            </select>
        </td>

        <td>
            <span class="label">Месяц:</span>
        </td>
        <td>
            <%--// ToDo сделать отдельный файл для формирования выпадашки с месяцами--%>
            <select data-dojo-id="monthreport_month" id="monthreport_month" onchange="monthReport_updateStatus();">
                <option id="monthreport_month_option_1"  value="1"  title="Январь">Январь</option>
                <option id="monthreport_month_option_2"  value="2"  title="Февраль">Февраль</option>
                <option id="monthreport_month_option_3"  value="3"  title="Март">Март</option>
                <option id="monthreport_month_option_4"  value="4"  title="Апрель">Апрель</option>
                <option id="monthreport_month_option_5"  value="5"  title="Май">Май</option>
                <option id="monthreport_month_option_6"  value="6"  title="Июнь">Июнь</option>
                <option id="monthreport_month_option_7"  value="7"  title="Июль">Июль</option>
                <option id="monthreport_month_option_8"  value="8"  title="Август">Август</option>
                <option id="monthreport_month_option_9"  value="9"  title="Сентябрь">Сентябрь</option>
                <option id="monthreport_month_option_10" value="10" title="Октябрь">Октябрь</option>
                <option id="monthreport_month_option_11" value="11" title="Ноябрь">Ноябрь</option>
                <option id="monthreport_month_option_12" value="12" title="Декабрь">Декабрь</option>
            </select>
        </td>
        <td>
            <span class="label">Статус:</span>
        </td>
        <td>
            <div id="monthReportStatus" data-dojo-id="monthReportStatus" style="font-weight: bold"></div>
        </td>
        <sec:authorize access="hasRole('ROLE_MONTH_REPORT_MANAGER')">
        <td>
            <button data-dojo-id="monthReport_closeButton" id="monthReport_closeButton" class="monthReportOpenClose"
                    onclick="monthReport_close();">Закрыть табель</button>
            <button data-dojo-id="monthReport_openButton" id="monthReport_openButton" class="monthReportOpenClose"
                    onclick="monthReport_open();">Открыть табель</button>
        </td>
        </sec:authorize>

    </tr>
</table>
<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
    <br>
    <button id="monthReport_saveButton"     data-dojo-id="monthReport_saveButton"     >Сохранить</button>
    <button id="monthReport_exportButton"   data-dojo-id="monthReport_exportButton"   >Экспорт в Эксель</button>
    <br>
</sec:authorize>
<br>
    <div data-dojo-id="tabContainer" data-dojo-type="dijit/layout/TabContainer" doLayout="false" id="tabContainer">
        <div id="monthReportTable_tab" data-dojo-type="dijit/layout/ContentPane" title="Табель" data-dojo-props="selected:true">
            <%@include file="monthReportTable.jsp" %>
        </div>
        <div id="overtimeTable_tab" data-dojo-type="dijit/layout/ContentPane" title="Переработки">
            <%@include file="overtimeTable.jsp" %>
        </div>
        <sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_MONTH_REPORT_MANAGER')">
            <div id="mutualWorkTable_tab" data-dojo-type="dijit/layout/ContentPane" title="Взаимная занятость">
                <%@include file="mutualWorkTable.jsp" %>
            </div>
        </sec:authorize>
    </div>

</body>
</html>