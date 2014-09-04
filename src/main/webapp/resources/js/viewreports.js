dojo.require("dojo.cookie");
dojo.require("dojo.on");
dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.form.FilteringSelect");
dojo.require("dojo.data.ObjectStore");
dojo.require("dojo.store.Memory");
dojo.require("dijit.Dialog");

var widgets = {
    division: undefined,
    month: undefined,
    year: undefined,
    employee: undefined
};

dojo.ready(function () {
    window.focus();
    initWidgets();
    updateEmployeeSelect();
    reloadViewReportsState();

    if (dojo.query('input[id^="delete_"]').length > 0) {
        var deleteAllCheckbox = dojo.byId("deleteAllCheckbox");

        dojo.connect(deleteAllCheckbox, "onclick", function (evt) {
            setAllCheckBoxChecked();
        });
    } else {
        dojo.query("#deleteAllCheckbox").style("display", "none");
    }

    dojo.on(widgets.division, "change", updateEmployeeSelect);

    dojo.connect(dijit.byId('sendDeleteReportDialog').closeButtonNode, "onclick", function (evt) {
        clearDeleteForm();
    });
});


function initWidgets() {
    widgets.division = dojo.byId('divisionId');
    widgets.month = dojo.byId('month');
    widgets.year = dojo.byId('year');
}

/* По умолчанию отображается текущий год и месяц. */
function reloadViewReportsState() {
    var temp_date = new Date();

    if (lastYear == 0 && lastMonth == 0) {
        widgets.year.value = temp_date.getFullYear();
        widgets.year.onchange();
        widgets.month.value = temp_date.getMonth() + 1;
    }
    else {
        widgets.year.value = lastYear;
        widgets.year.onchange();
        widgets.month.value = lastMonth;
    }
}

function updateEmployeeSelect() {
    var divisionId =  widgets.division.value;

    dojo.xhrGet({
        url: getContextPath() + "/employee/employeeListWithLastWorkday/" + divisionId + "/true/true",
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

    var employeeFilteringSelect = dijit.byId("employeeId");

    if (!employeeFilteringSelect) {
        employeeFilteringSelect = new dijit.form.FilteringSelect({
            id: "employeeId",
            name: "employeeId",
            store: employeeDataStore,
            searchAttr: 'value',
            queryExpr: "*\${0}*",
            ignoreCase: true,
            autoComplete: false,
            style: 'width:200px',
            required: true,
            onChange: function () {
                onChangeEmployee();
            },
            onMouseOver: function () {
                tooltip.show(getTitle(this));
            },
            onMouseOut: function () {
                tooltip.hide();
            }
        }, "employeeId");
        employeeFilteringSelect.startup();
        widgets.employee = employeeFilteringSelect;
    } else {
        employeeFilteringSelect.set('store', employeeDataStore);
        widgets.employee.set('value', null);
    }


    widgets.employee.set('value', employeeIdJsp);
}

function onChangeEmployee() {
    if (widgets.employee.item && !isUndefinedNullNaN(widgets.employee.item.birthday) && widgets.employee.item.birthday != ''){
        dojo.byId('employeeBirthday').innerHTML = widgets.employee.item.birthday;
        dojo.byId('employeeBirthday').hidden = false;
        dojo.byId('employeeBirthdayLabel').hidden = false;
    }else{
        dojo.byId('employeeBirthday').hidden = true;
        dojo.byId('employeeBirthdayLabel').hidden = true;
    }
    setDefaultEmployeeJob(-1);
}

function incMonth() {
    var month = widgets.month.value;
    if (month < 12) {
        month++;
        widgets.month.value = month;
        showDates()
    }
}

function decMonth() {
    var month = widgets.month.value;
    if (month > 1) {
        month--;
        widgets.month.value = month;
        showDates()
    }
}

function incYear() {
    var length = widgets.year.options.length;
    var year = widgets.year.value;
    if (year < widgets.year.options.item(length - 1).value) {
        year++;
        widgets.year.value = year;
        showDates()
    }
}

function decYear() {
    var year = widgets.year.value;
    if (year > widgets.year.options.item(0).value) {
        year--;
        widgets.year.value = year;
        showDates()
    }
}

function showDates() {
    var empId = widgets.employee.item != null ? widgets.employee.item.id : null;
    var year = widgets.year.value;
    var month = widgets.month.value;

    var error = "";
    if (year == 0 || year == null) {
        error += ("Необходимо выбрать год и месяц!\n");
    }
    if (month == 0 || month == null) {
        error += ("Необходимо выбрать месяц!\n");
    }
    if (empId == 0 || empId == null) {
        error += ("Необходимо выбрать сотрудника!\n");
    }

    if (error) {
        alert(error);
        return;
    }

    viewReportsForm.action = getContextPath() + "/viewreports/" + empId + "/" + year + "/" + month;
    viewReportsForm.submit();
}

function yearChange(obj) {
    var year = null;
    var monthSelect = widgets.month;
    var monthValue = monthSelect.value;
    var monthOption = null;
    if (obj.target == null) {
        year = obj.value;
    }
    else {
        year = obj.target.value;
    }
    //Очищаем список месяцев.
    monthSelect.options.length = 0;
    for (var i = 0; i < monthList.length; i++) {
        if (year == monthList[i].year) {
            for (var j = 0; j < monthList[i].months.length; j++) {
                if (monthList[i].months[j].number != 0 && monthList[i].months[j].number != 27) {
                    monthOption = dojo.doc.createElement("option");
                    dojo.attr(monthOption, {value: monthList[i].months[j].number});
                    monthOption.title = monthList[i].months[j].name;
                    monthOption.innerHTML = monthList[i].months[j].name;
                    monthSelect.appendChild(monthOption);
                }
            }
        }
    }
    monthSelect.value = monthValue;
    if (year == 0) {
        insertEmptyOption(monthSelect);
    }
}

function setIdsToForm() {
    var idsField = dojo.byId("ids");
    var ids = [];
    var idString = "delete_";
    var deleteIdsElements = dojo.query('input[id^="' + idString + '"]');
    for (var index = 0; index < deleteIdsElements.length; ++index) {
        var element = deleteIdsElements[index];
        if (element.checked) {
            ids.push(element.id.substring(idString.length, element.id.length));
        }
    }
    if (ids.length == 0) {
        alert('Не выделено ни одного отчета');
        return false;
    }
    idsField.value = ids;
    return true;
}

function deleteSelectedReports() {
    if (!setIdsToForm()) {
        return;
    }

    if (!confirm("Вы действительно хотите удалить выделенные отчеты")) {
        return;
    }
    processing();
    var deleteForm = dojo.byId("deleteReportsForm");
    dojo.byId("link").value = document.URL;
    deleteForm.action = getContextPath()+"/deleteReports";
    deleteForm.submit();
}

function sendToRawReports() {
    if (!setIdsToForm()) {
        return;
    }

    if (!confirm("Вы действительно хотите отправить выделенные отчеты в черновик")) {
        return;
    }
    processing();
    var deleteForm = dojo.byId("deleteReportsForm");
    dojo.byId("link").value = document.URL;
    deleteForm.action = getContextPath()+"/sendToRawReports";
    deleteForm.submit();
}

function setAllCheckBoxChecked() {
    var idString = "delete_";
    var deleteIdsElements = dojo.query('input[id^="' + idString + '"]');
    var deleteAllCheckbox = dojo.byId("deleteAllCheckbox");
    for (var index = 0; index < deleteIdsElements.length; ++index) {
        var element = deleteIdsElements[index];
        element.checked = deleteAllCheckbox.checked;
    }
}

function openVacation(date, emplId, divId) {
    var vacationForm = dojo.byId("vacationsForm");
    vacationForm.action = getContextPath()+"/vacations";

    dojo.byId("calFromDate").value = date;
    dojo.byId("calToDate").value = date;
    dojo.query("#vacationsForm > #employeeId")[0].value = emplId;
    dojo.query("#vacationsForm > #divisionId")[0].value = divId;
    vacationForm.submit();
}

function sendDeleteTimeSheetApproval(reportId) {
    dojo.byId("link").value = document.URL;
    dojo.byId("reportId").value = reportId;
    var dialog = dijit.byId("sendDeleteReportDialog");
    dialog.show();
    return;
}

function clearDeleteForm() {
    dojo.byId("link").value = null;
    dojo.byId("reportId").value = null;
    dojo.byId("deleteRB").checked = true;
    dojo.byId('commentApproval').value = "";
}

function submitDeleteApproval() {
    var dialog = dijit.byId("sendDeleteReportDialog");
    dialog.hide();
    dojo.byId('comment').value = dojo.byId('commentApproval').value;
    var deleteForm = dojo.byId("deleteReportsForm");
    var value = dojo.query('input[name=deleteGroup]:checked').attr('value')[0];
    deleteForm.action = getContextPath()+(value == "delete" ? "/sendDeleteReportApproval" : "/setDraftReportApproval");
    deleteForm.submit();
}

function showApprovalDialog(comment) {
    var dialog = dijit.byId("showApprovaldialog");
    dojo.byId('commentText').value = comment;
    dialog.show();
}

function closeShowApprovalDialog() {
    var dialog = dijit.byId("showApprovaldialog");
    dialog.hide();
}
