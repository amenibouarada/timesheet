/**
 * Created with IntelliJ IDEA.
 * User: rshamsutdinov
 * Date: 22.01.13
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */


function showGraphic(type) {

    // запомним режим просмотра
    var selectedTabInput = dojo.byId(VIEW_MODE);
    selectedTabInput.value = type;

    if (vacationListJSON.length == 0) { // если нет данных для отображения
        dojo.byId("emptyMessage").innerHTML = "Нет данных для отображения";
        return;
    }
    dojo.byId("emptyMessage").innerHTML = "";

    if (type == VIEW_TABLE) { // если режим отображения - таблица
        if (dojo.byId("byDay").checked) { // то смотрим, какой переключатель стоит
            type = VIEW_GRAPHIC_BY_DAY;
        } else {
            type = VIEW_GRAPHIC_BY_WEEK;
        }
    }
    var g = new Gantt(dojo.byId("graphic_div"), holidayList, type, dojo.byId(CAL_FROM_DATE).value, dojo.byId(CAL_TO_DATE).value);

    for (var i in vacationListJSON) {
        var vacation = new RegionEmployees(vacationListJSON[i].region_name, vacationListJSON[i].employeeList, vacationListJSON[i].holidays);
        g.AddRegionEmployeeList(vacation);
    }

    //отрисовка только самой таблицы
    g.Draw();

    // растянем контейнер вкладок
    var tableGraphicWidth = dojo.byId("tableGraphic").clientWidth * 1.1;
    // если меньше ширины экрана - то трогать не будем
    if (document.body.clientWidth < tableGraphicWidth) {
        dojo.attr(dojo.byId("tabContainer"), {
            style: "width: " + tableGraphicWidth + "px"
        });
    }
}

function showVacations() {
    dojo.byId(VACATION_ID).setAttribute("disabled", "disabled");
    vacationsForm.action = contextPath + "/vacations";
    vacationsForm.submit();
}

function divisionChangeVac(division) {
    var divisionId = division.value;
    if (divisionId == undefined) {
        divisionId = division;
    }
    fillProjectListByDivChange(divisionId);
    sortManager();
    updateEmployeeSelect();
}

function multipleOptSelectedInSelect(select) {
    var allOptionIndex;

    var isAllOption = dojo.some(select.options, function (option, idx) {
        if (option.value == ALL_VALUE && option.selected) {
            allOptionIndex = idx;
            return true;
        }

        return false;
    });

    if (isAllOption) {
        select.removeAttribute("multiple");
        select.selectedIndex = allOptionIndex;
    } else {
        select.setAttribute("multiple", "multiple");
    }
    updateEmployeeSelect();
}

function sortManager() {
    var divisionId = dojo.byId(DIVISION_ID).value;
    var managerSelect = dojo.byId(MANAGER_ID);
    managerSelect.options.length = 0;
    insertEmptyOptionWithCaptionInHead(managerSelect, "Все");

    for (var i = 0; i < managerList.length; i++) {
        if (managerList[i].divId == divisionId) {
            var managerOption = dojo.doc.createElement("option");
            dojo.attr(managerOption, {
                value: managerList[i].id
            });
            managerOption.title = managerList[i].value;
            managerOption.innerHTML = managerList[i].value;
            managerSelect.appendChild(managerOption);
        }
    }
}

function updateEmployeeSelect() {
    dojo.xhrGet({
        url: getContextPath() + "/vacations/getEmployeeList",
        form: "vacationsForm",
        handleAs: "json",
        timeout: 10000,
        load: function (employeeList) {
            var employeeArray = [];
            var emptyObj = {
                id: 0,
                value: ""
            };
            employeeArray.push(emptyObj);
            dojo.forEach(employeeList, function (employee) {
                employeeArray.push(employee);
            });

            employeeArray.sort(function (a, b) {
                return (a.value < b.value) ? -1 : 1;
            });

            employeeArray[0].value = "Все сотрудники";

            var employeeDataStore = new dojo.data.ObjectStore({
                objectStore: new dojo.store.Memory({
                    data: employeeArray,
                    idProperty: 'id'
                })
            });

            var employeeFlteringSelect = dijit.byId("employeeIdSelect");

            if (!employeeFlteringSelect) {
                employeeFlteringSelect = new dijit.form.FilteringSelect({
                    id: "employeeIdSelect",
                    labelAttr: "value",
                    store: employeeDataStore,
                    searchAttr: 'value',
                    queryExpr: "*\${0}*",
                    ignoreCase: true,
                    autoComplete: false,
                    style: 'width:200px',
                    required: true,
                    onMouseOver: function () {
                        tooltip.show(getTitle(this));
                    },
                    onMouseOut: function () {
                        tooltip.hide();
                    },
                    onChange: function () {
                        var selectedEmployee2 = this.item ? this.item.id : null;
                        dojo.byId('employeeId').value = selectedEmployee2;
                        selectedEmployee = selectedEmployee2;
                    }
                }, "employeeIdSelect");
                employeeFlteringSelect.startup();
            } else {
                employeeFlteringSelect.set('store', employeeDataStore);
                dijit.byId("employeeIdSelect").set('value', null);
                dojo.byId('employeeId').value = null;
            }
            dijit.byId("employeeIdSelect").set('value', selectedEmployee);
            dojo.byId('employeeId').value = selectedEmployee;
        }
    });
}

function addVacation() {
    var empId = dojo.byId(EMPLOYEE_ID).value;
    if (!empId || empId == "null") {
        alert("Не выбрано значение в поле \"Сотрудник\"");
        return;
    }
    vacationsForm.action = contextPath + "/createVacation/" + empId;
    vacationsForm.submit();
}

function deleteVacation(parentElement, vac_id) {
    if (!confirm("Удалить заявку?")) {
        return;
    }

    dojo.byId(VACATION_ID).removeAttribute("disabled");
    dojo.byId(VACATION_ID).value = vac_id;
    vacationsForm.action = contextPath + "/vacations";
    vacationsForm.submit();
}

function approveVacation(vac_id, beginDate, endDate, type) {
    if (!confirm("Вы действительно хотите окончательно согласовать отпуск типа " +
        "\"" + type + "\" " +
        "на период: " + beginDate + " - " + endDate + " ?")) {
        return;
    }

    dojo.xhrGet({
        url: contextPath + "/approveVacation",
        handleAs: "text",
        timeout: 10000,
        sync: true,
        content: { vacationId: vac_id},
        preventCache: true,
        load: function (data) {
            var jsonData = dojo.fromJson(data);
            console.log(jsonData);
            if (jsonData.isApproved) {
                document.location = contextPath + "/vacations";
            } else {
                alert(jsonData.message);
            }
        },
        error: function (err) {
            console.log(err);
            alert("Во время операции произошла ошибка");
        }
    });
}

function deleteApprover(apr_id) {
    if (!confirm("Удалить утверждающего?")) {
        return;
    } else {
//        console.log("apr_id = " + apr_id);
        dojo.byId(APPROVAL_ID).value = apr_id;
        vacationsForm.action = contextPath + "/vacations";
        vacationsForm.submit();
    }
}

/* Заполняет список доступных проектов/пресейлов */
function fillProjectListByDivChange(division) {

    var projectSelect = dojo.byId(PROJECT_ID);
    dojo.removeAttr(projectSelect, "disabled");
    //Очищаем список проектов.
    projectSelect.options.length = 0;
    var hasAny = false;
    for (var i = 0; i < fullProjectList.length; i++) {
        if (division == 0 || fullProjectList[i].divId == division) {
            var divProjs = fullProjectList[i].divProjs;
            for (var j = 0; j < divProjs.length; j++) {
                projectOption = dojo.doc.createElement("option");
                dojo.attr(projectOption, {
                    value: divProjs[j].id
                });
                projectOption.title = divProjs[j].value;
                projectOption.innerHTML = divProjs[j].value;
                projectSelect.appendChild(projectOption);
                hasAny = true;
            }
            break;
        }
    }

    sortSelectOptions(projectSelect);
    validateAndAddNewOption(hasAny, division, projectSelect);
    projectSelect.value = 0;
}

function validateAndAddNewOption(hasAny, divisionId, select) {
    if (hasAny || divisionId == 0) {
        insertEmptyOptionWithCaptionInHead(select, "Все");
    } else {
        insertEmptyOptionWithCaptionInHead(select, "Пусто");
        dojo.attr(select, {disabled: "disabled"});
    }
}
