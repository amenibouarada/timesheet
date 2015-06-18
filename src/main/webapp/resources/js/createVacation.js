dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.form.FilteringSelect");
dojo.require("dojo.data.ObjectStore");
dojo.require("dojo.store.Memory");
dojo.require("dojo.on");
dojo.require(CALENDAR_EXT_PATH);

dojo.ready(function () {
    window.focus();
    dojo.byId("divisionId").value = divisionIdJsp;
    updateEmployeeSelect();
    dojo.byId("employeeId").value = employeeIdJsp;
    dojo.connect(dojo.byId("divisionId"), "onchange", dojo.byId("divisionId"), updateEmployeeSelect);
    dojo.connect(dojo.byId("divisionId"), "onchange", dojo.byId("divisionId"), updateExitToWorkAndCountVacationDay);
    dojo.connect(dojo.byId("vacationType"), "onchange", dojo.byId("vacationType"), updateExitToWorkAndCountVacationDay);
    dojo.connect(dojo.byId("vacationType"), "onchange", dojo.byId("vacationType"), updateSubmitButton);
    dojo.connect(dojo.byId("vacationType"), "onchange", dojo.byId("vacationType"), updateCountVacationDaysForPeriod);
    dojo.on(dijit.byId("calFromDate"), "change", updateExitToWorkAndCountVacationDay);
    dojo.on(dijit.byId("calFromDate"), "change", updateCountVacationDaysForPeriod);
    dojo.on(dijit.byId("calToDate"), "change", updateExitToWorkAndCountVacationDay);
    initCurrentDateInfo(employeeIdJsp, dijit.byId('calFromDate').value, getUrl());
});

dojo.declare("Calendar", com.aplana.dijit.ext.Calendar, {
    getEmployeeId: getEmployeeId,
    getClassForDateInfo: function (dateInfo, date) {
        switch (dateInfo) {
            case "2":   //выходной или праздничный день
                return 'classDateRedText';
                break;
            case "3":   //в этот день имеется отпуск
                return 'classDateRedBack';
                break;
            case "4":   //в этот день имеется планируемый отпуск
                return 'classDateBlueBack';
                break;
            case "5":   //в этот день имеется пересечение планируемого и реального отпуска
                return 'classDateVioletBack';
                break;
            case "0":   //день без отпуска
                if (date <= getFirstWorkDate(dijit.byId("employeeIdSelect").item)) {// день раньше начала работы
                    return '';
                } else {
                    return 'classDateGreen';
                }
            default: // Никаких классов не назначаем, если нет информации
                return '';
                break;
        }
    }
});

dojo.declare("DateTextBox", com.aplana.dijit.ext.DateTextBox, {
    popupClass: "Calendar", isDisabledDate: function (date) {
        var typeDay = new Number(getTypeDay(date));
        if (dojo.byId("types").value === typeVacPlanned) {
            if (typeDay == 4 || typeDay == 3 || typeDay == 5) { //если выбран тип отпуска планируемый
                // и имеется пересечение планируемого и обычного отпуска
                // или планируемый или обычный то делаем ячейку недоступной
                return true;
            }
        } else if (typeDay == 3 || typeDay == 5) {  //если в этот день отпуск или
            // пересечение отпусков - делаем ячейку недоступной
            return true;
        } else {
            if (hasRoleAdmin) {
                return (date <= new Date());
            } else {
                return false;
            }
        }
    }
});

require(["dijit/Tooltip", "dojo/domReady!"], function (Tooltip) {
    new Tooltip({
        connectId: ["calToDateToolTip", "calFromDateToolTip"],
        label: "<table class='without_borders'>" +
            "<tr><td><div class='blockTooltip classDateGreen'> </div></td><td><div style='padding: 5px;'> - эти дни доступны для оформления отпуска</div></td></tr>" +
            "<tr><td><div class='blockTooltip classDateRedBack'> </div></td><td> <div style='padding: 5px;'> - эти дни недоступны для оформления отпуска (имется отпуск)</div> </td></tr>" +
            "<tr><td><div class='blockTooltip classDateBlueBack'> </div></td><td> <div style='padding: 5px;'> - в эти дни запланирован отпуск</div> </td></tr>" +
            "<tr><td><div class='blockTooltip classDateVioletBack'> </div></td><td> <div style='padding: 5px;'> - эти дни недоступны для оформления <br> " +
            "отпуска (имется обычный и запланированный отпуск)</div> </td></tr>" +
            "<table>"
    });
});

function getEmployeeId() {
    return dojo.byId("employeeId").value;
}

function getUrl() {
    if (dojo.byId("types").value === typeVacPlanned) {
        return '/calendar/vacationDatesPlanned';
    } else {
        return '/calendar/vacationDates';
    }
}

function setDate(date_picker, date) {
    date_picker.set("displayedValue", date);
}

function createVacation(approved) {
    processing();
    dojo.byId("createVacationId").disabled = true;
    var createVacAdminBtn = dojo.byId("createApprovedVacationId");
    //Может быть null если пользователь не с админискими правами
    if (createVacAdminBtn) createVacAdminBtn.disabled = true;

    var empId = dojo.byId("employeeId").value;
    if (validate()) {
        createVacationForm.action = getContextPath() + "/validateAndCreateVacation/" + empId + "/" + (approved ? "1" : "0");
        createVacationForm.submit();
    } else {
        stopProcessing();
        dojo.byId("createVacationId").disabled = false;
        if (createVacAdminBtn) createVacAdminBtn.disabled = false;
    }
}

function validate() {
    var fromDate = dijit.byId("calFromDate").get('value');
    var toDate = dijit.byId("calToDate").get('value');
    var type = dojo.byId("types").value;
    var comment = dojo.byId("comment").value;

    var error = "";

    if (isNilOrNull(fromDate)) {
        error += "Необходимо указать дату начала отпуска\n";
    }

    if (isNilOrNull(toDate)) {
        error += "Необходимо указать дату окончания отпуска\n";
    }

    if (fromDate > toDate) {
        error += "Дата начала отпуска не может быть больше даты окончания\n";
    }

    if (isNilOrNull(type)) {
        error += "Необходимо указать тип отпуска\n";
    }

    if (type == typeWithRequiredCommentJsp && comment.length == 0) {
        error += "Необходимо написать комментарий\n";
    }

    if (!checkVacation()) {
        return false;
    }

    if (error.length == 0) {
        return true;
    }

    alert(error);

    return false;
}

function checkVacation() {
    // ToDo реализовать позднее, когда появится необходимость
    return true;



    var vacationType = dojo.byId("types").value;
    var errorField = dojo.byId("errorField");
    if (vacationType != EnumConstants.VacationTypesEnum.WITH_PAY && vacationType != EnumConstants.VacationTypesEnum.PLANNED) {
        errorField.innerHTML = "";
        return true;
    }

    var result = true;
    var fromDate = dojo.byId("calFromDate").value;
    var endDate = dojo.byId("calToDate").value;
    processing();
    dojo.xhrGet({
        url: getContextPath() + "/checkVacationCountDays",
        handleAs: "json",
        sync: true,
        content: {
            beginDate: fromDate,
            endDate: endDate,
            employeeId: getEmployeeId(),
            vacationTypeId: vacationType
        },

        load: function (data) {
            stopProcessing();
            errorField.innerHTML = "";
            if (data.error == -1) {
                alert(data.message);
                result = false;
            } else if (data.error == 1) {
                result = confirm(data.message);
            } else if (data.error == 0) {
                result = true;
            }
        },

        error: function (error) {
            stopProcessing();
            handleError(error.message);
            errorField.setAttribute("class", "error");
            errorField.innerHTML = "Не удалось выполнить проверку на оставшиеся дни отпуска";
            result = false;
        }
    });

    return result;
}

function updateCountVacationDaysForPeriod() {
    // ToDo реализовать позднее, когда появится необходимость
    return true;

    var countDaysElement = dojo.byId("countDays");
    var fromDate = dojo.byId("calFromDate").value;
    var vacationType = dojo.byId("types").value;
    if (vacationType != EnumConstants.VacationTypesEnum.WITH_PAY && vacationType != EnumConstants.VacationTypesEnum.PLANNED) {
        countDaysElement.innerHTML = "";
        return;
    }
    countDaysElement.innerHTML = loadImg;
    dojo.xhrGet({
        url: getContextPath() + "/getCountVacationDayForPeriod",
        handleAs: "json",
        content: {
            beginDate: fromDate,
            employeeId: getEmployeeId(),
            vacationTypeId: vacationType
        },
        load: function (data) {
            countDaysElement.setAttribute("class", "");
            var count = (data.vacation_days_count != null ? data.vacation_days_count : 0);
            countDaysElement.innerHTML = "";
            countDaysElement.innerHTML = "Количество доступных дней отпуска: " + count;
        },

        error: function (error) {
            handleError(error.message);
            countDaysElement.setAttribute("class", "error");
            countDaysElement.innerHTML = "Не удалось получить количество доступных дней отпуска!";
        }
    });
}

function updateExitToWorkAndCountVacationDay() {
    var fromDate = dojo.byId("calFromDate").value;
    var endDate = dojo.byId("calToDate").value;
    var vacationType = dojo.byId("types").value;
    var exitToWorkElement = dojo.byId("exitToWork");

    if ((typeof fromDate == typeof undefined || fromDate == null || fromDate.length == 0)
        || (typeof endDate == typeof undefined || endDate == null || endDate.length == 0)
        || (typeof vacationType == typeof undefined || vacationType == null)) {
        exitToWorkElement.innerHTML = '';
    } else {

        exitToWorkElement.innerHTML = loadImg;

        dojo.xhrGet({
            url: getContextPath() + "/getExitToWorkAndCountVacationDay",
            handleAs: "json",
            content: {
                beginDate: fromDate,
                endDate: endDate,
                employeeId: getEmployeeId(),
                vacationTypeId: vacationType
            },
            load: function (data) {
                if (data.size != 0) {
                    exitToWorkElement.setAttribute("class", "");
                    if (data.error != undefined) {
                        var errorField = dojo.byId("errorField");
                        errorField.innerHTML = "<div style='background: #F9F7BA;padding: 5px;color: #F00;'>" + data.error + "</div>";
                        exitToWorkElement.innerHTML = "";
                        if (dojo.byId("createVacationForm.errors") != undefined) {
                            dojo.destroy("createVacationForm.errors");
                        }
                    } else {
                        var errorField = dojo.byId("errorField");
                        errorField.innerHTML = "";
                        exitToWorkElement.innerHTML = "Количество рабочих дней в отпуске :" + data.vacationWorkDayCount +
                            "<br>Количество дней в отпуске :" + data.vacationDayCount +
                            "<br>Дата выхода на работу: " + data.exitDate;
                        if (data.vacationFridayInform) {
                            exitToWorkElement.innerHTML += "<br><b><i>Отпуск необходимо оформлять с понедельника по воскресенье</i></b>";
                        }
                    }
                } else {
                    exitToWorkElement.innerHTML = "Не удалось получить дату выхода из отпуска!";
                }
            },

            error: function (error) {
                handleError(error.message);
                exitToWorkElement.setAttribute("class", "error");
                exitToWorkElement.innerHTML = "Не удалось получить дату выхода из отпуска!";
            }
        });
    }
}

function cancel() {
    window.location = getContextPath() + "/vacations";
}
function openCreateVacationRules() {

    window.open(rulesUrl);
}

function updateSubmitButton() {
    if (dojo.byId('types').value == childBearId || dojo.byId('types').value == childCareId) {
        dojo.byId('createVacationId').setAttribute("onclick", "javascript: createVacation(true)");
    } else {
        dojo.byId('createVacationId').setAttribute("onclick", "javascript: createVacation(false)");
    }

}

function updateEmployeeSelect() {
    var divisionId = dojo.byId('divisionId').value;
    dojo.xhrGet({
        url: getContextPath() + "/employee/employeeListWithLastWorkday/" + divisionId + "/false/false",
        handleAs: "json",
        timeout: 10000,
        sync: true,
        preventCache: false,
        headers: {  'Content-Type': 'application/json;Charset=UTF-8',
            "Accept": "application/json;Charset=UTF-8"},
        load: function (data) {
            refreshEmployeeSelect(data);
        },

        error: function (error) {
            console.log(error);
            handleError(error.message);
        }
    });


}

function refreshEmployeeSelect(employeeList) {
    employeeList.sort(function (a, b) {
        return (a.value < b.value) ? -1 : 1;
    });

    var employeeDataStore = new dojo.data.ObjectStore({
        objectStore: new dojo.store.Memory({
            data: employeeList,
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
                var value = this.item ? this.item.id : null;
                dojo.byId('employeeId').value = value;
                dateInfoHolder = [];
                updateExitToWorkAndCountVacationDay();
                updateCountVacationDaysForPeriod();
            }
        }, "employeeIdSelect");
        employeeFlteringSelect.startup();
    } else {
        employeeFlteringSelect.set('store', employeeDataStore);
        dijit.byId("employeeIdSelect").set('value', null);

    }

    dijit.byId("employeeIdSelect").set('value', employeeIdJsp);
}
