// пересчет показателей планов
function recalcColumns(myStoreObject, inRowIndex) {

    var lastValue;
    var sumProject = 0;
    var sumPresale = 0;
    var sumProjectCenter = 0;
    var sumPresaleCenter = 0;
    var sumInvest = 0;
    var sumComercial = 0;

    dojo.forEach(projectList, function (project) {
        var projectPlan = myStoreObject.items[inRowIndex][project.id + _PLAN][0] * 1;

        if (project.project_type == PRESALE){
            sumPresaleCenter += projectPlan;
            sumPresale += projectPlan;
        }else{
            sumProject += projectPlan;
            sumProjectCenter += projectPlan;
        }

        var projectDivision = project.project_division;
        // Если не указано, то это ЦЗР !!
        if (projectDivision == -1) {
            projectDivision = 1;
        }
        if (projectDivision != dojo.byId(DIVISION_ID).value || project.project_funding_type == COMMERCIAL_PROJECT){
            sumComercial += projectPlan;
        }else{
            sumInvest += projectPlan;
        }
    });
    // Проекты центра
    myStoreObject.items[inRowIndex][CENTER_PROJECTS + _PLAN][0] = sumProjectCenter;

    // Пресейлы центра
    myStoreObject.items[inRowIndex][CENTER_PRESALES + _PLAN][0] = sumPresaleCenter;

    // Проекты
    myStoreObject.items[inRowIndex][SUMMARY_PROJECTS + _PLAN][0] = sumProject +
            myStoreObject.items[inRowIndex][OTHER_PROJECT + _PLAN][0];

    // Пресейлы
    myStoreObject.items[inRowIndex][SUMMARY_PRESALES + _PLAN][0] = sumPresale +
            myStoreObject.items[inRowIndex][OTHER_PRESALE + _PLAN][0];

    if (showSumFundingType){
        // Инвестиционные активности
        myStoreObject.items[inRowIndex][SUMMARY_INVESTMENT + _PLAN][0] = sumInvest +
            myStoreObject.items[inRowIndex][OTHER_INVEST_PROJECT + _PLAN][0];

        // Коммерческие активности
        myStoreObject.items[inRowIndex][SUMMARY_COMMERCIAL + _PLAN][0] = sumComercial +
            myStoreObject.items[inRowIndex][OTHER_COMERCIAL_PROJECT + _PLAN][0] +
            myStoreObject.items[inRowIndex][NON_PROJECT + _PLAN][0] * 1;
    }

    // Итог, %
    var percent_of_charge =
            myStoreObject.items[inRowIndex][CENTER_PROJECTS + _PLAN][0] * 1 +
            myStoreObject.items[inRowIndex][CENTER_PRESALES + _PLAN][0] * 1 +
            myStoreObject.items[inRowIndex][OTHER_PROJECTS_AND_PRESALES + _PLAN][0] * 1 +
            myStoreObject.items[inRowIndex][NON_PROJECT + _PLAN][0] * 1 +
            myStoreObject.items[inRowIndex][ILLNESS + _PLAN][0] * 1 +
            myStoreObject.items[inRowIndex][VACATION + _PLAN][0] * 1  ;
    myStoreObject.items[inRowIndex][PERCENT_OF_CHARGE + _PLAN][0] = percent_of_charge;

    // Итог, ч
    var monthPlan = myStoreObject.items[inRowIndex][MONTH_PLAN][0];
    myStoreObject.items[inRowIndex][SUMMARY + _PLAN][0] = Math.round(monthPlan * percent_of_charge / 100) + "/" + monthPlan;
}

function getCookieValue(CookieName) {
    var razrez = document.cookie.split(CookieName + '=');
    if (razrez.length > 1) { // Значит, куки с этим именем существует
        var hvost = razrez[1],
            tzpt = hvost.indexOf(';'),
            EndOfValue = (tzpt > -1) ? tzpt : hvost.length;
        return unescape(hvost.substring(0, EndOfValue));
    }
}

function updateManagerList(id) {
    if (id == null) {
        id = dojo.byId(DIVISION_ID).value;
    }
    var managersNode = dojo.byId(MANAGER);
    var regionsNode = dojo.byId(REGIONS);

    var manager = managersNode.value;
    /* очищаем список */
    managersNode.options.length = 0;
    /* приклеиваем 'всех руководителей' */
    insertEmptyOptionWithCaptionInHead(managersNode, "Все руководители");

    var isAllOption = dojo.some(regionsNode.options, function (option, idx) {
        if (option.value == ALL_VALUE && option.selected) {
            return true;
        }
        return false;
    });
    var selectedRegions;
    if (!isAllOption) {
        selectedRegions = getRegionsSelected();
    }

    var count = 0;
    if (managerMapJson.length > 0) {
        var managerMap = dojo.fromJson(managerMapJson);
        dojo.forEach(dojo.filter(managerMap, function (m) {
            return (m.division == id);
        }), function (managerData) {
            if (isAllOption) {
                var option = document.createElement("option");
                dojo.attr(option, {
                    value:managerData.id
                });
                option.text = managerData.name;
                option.innerHTML = managerData.name;
                if (managerData.number == manager) {
                    option.selected = "selected";
                }
                managersNode.appendChild(option);
            } else {
                var add = false;
                dojo.forEach(managerData.regionWhereMan,
                    function (redData) {
                        for (var i = 0; i < selectedRegions.length; i++) {
                            if (selectedRegions[i] == redData.id) {
                                add = true;
                            }
                        }
                    }
                );

                if (add) {
                    var option = document.createElement("option");
                    dojo.attr(option, {
                        value:managerData.id
                    });
                    option.text = managerData.name;
                    option.innerHTML = managerData.name;
                    if (managerData.number == manager) {
                        option.selected = "selected";
                    }
                    managersNode.appendChild(option);
                }
            }
        });
    }
    if (managersNode.options.length == 1 && emptyOption.value == managersNode.options[0].value) {
        dojo.byId(MANAGER).disabled = 'disabled';
    } else {
        dojo.byId(MANAGER).disabled = '';
    }
}

function updateMonthList(year) {
    var monthNode = dojo.byId(MONTH);
    var month = monthNode.value;
    monthNode.options.length = 0;
    if (monthMapJson.length > 0) {
        var monthMap = dojo.fromJson(monthMapJson);
        dojo.forEach(dojo.filter(monthMap, function (monthData) {
            return (monthData.year == year);
        }), function (monthData) {
            dojo.forEach(monthData.months, function (monthObj) {
                var option = document.createElement("option");
                dojo.attr(option, {
                    value:monthObj.number
                });
                option.text = monthObj.name;
                option.innerHTML = monthObj.name;
                if (monthObj.number == month) {
                    option.selected = "selected";
                }
                monthNode.appendChild(option);
            });
        });
    }
}

function updateMultipleForSelect(select) {
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
}

function getRegionsSelected() {
    var regionsNode = dojo.byId(REGIONS);
    var regionsSelected = [];
    for (var i = 0; i < regionsNode.length; i++) {
        if (regionsNode.options[i].selected) {
            regionsSelected.push(regionsNode.options[i].value);
        }
    }
    return regionsSelected;
}

function validate() {
    var errors = [];

    if (dojo.byId(DIVISION_ID).value == 0) {
        errors.push("Не выбрано подразделение");
    }

    if (dojo.byId(YEAR).value == 0) {
        errors.push("Не выбран год");
    }

    if (dojo.byId(MONTH).value == 0) {
        errors.push("Не выбран месяц");
    }

    if (!dojo.byId(REGIONS).value) {
        errors.push("Не выбран ни один регион");
    }

    if (!dojo.byId(PROJECT_ROLES).value) {
        errors.push("Не выбрана ни одна должность");
    }
    if ((!dojo.byId(SHOW_PLANS).checked )
        && (!dojo.byId(SHOW_FACTS).checked )) {
        errors.push("Необходимо выбрать плановые или фактические показатели для отображения");
    }
    return !showErrors(errors);
}

function exportTableInExcel() {
    var year = dojo.byId("year").value;
    var month = dojo.byId("month").value;
    var form = dojo.byId(FORM);
    form.action = (getContextPath() + EXPORT_TABLE_EXCEL + "/" + year + "/" + month);
    form.submit();

    form.action = getContextPath() + PLAN_EDIT_URL;
}

function checkChanges() {
    if (hasChanges) {
        return "Изменения не были сохранены.";
    }
}

function replacePeriodsWithDots(value) {
    if (typeof value == "string") {
        value = value.replace(/,/, ".");
    }

    return value;
}

function log(text) {
    console.log(text);
}