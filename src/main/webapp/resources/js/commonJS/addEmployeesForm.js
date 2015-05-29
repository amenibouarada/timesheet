// Обновляет список сотрудников на форме добавления сотрудников
function updateAdditionEmployeeList() {
    var divisionId = dojo.byId("divisionId").value;
    var managerId = dojo.byId("managerId").value;
    var projectRoleListId = getSelectValues(dojo.byId("projectRoleListId"));
    var regionListId = getSelectValues(dojo.byId("regionListId"));

    // Делает ajax запрос, возвращающий сотрудников по центру/руководителю/должности/региону,
    processing();
    dojo.xhrGet({
        url: "/employmentPlanning/getAddEmployeeListAsJSON",
        content: {
            divisionId: divisionId,
            managerId: managerId,
            projectRoleListId: projectRoleListId,
            regionListId: regionListId
        },
        handleAs: "text",
        load: function (response, ioArgs) {
            updateEmployeeList(response);
            stopProcessing();
        },
        error: function (response, ioArgs) {
            stopProcessing();
            alert('При запросе списка сотрудников произошла ошибка. ' + PROBLEM_SOLVING_MESSAGE);
        }
    });

    function updateEmployeeList(response) {
        var grid = dijit.byId("projectGrid");
        var employeeSelect = dojo.byId("additionEmployeeList");
        employeeSelect.options.length = 0;

        grid.store.fetch({query: {}, onComplete: checkExists, queryOptions: {deep: true}});

        // Те люди, которые уже есть в гриде - дизейблятся
        function checkExists(items) {
            var map = [];

            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                map[item["employee_id"]] = item;
            }

            var selectSize = 0;
            dojo.forEach(dojo.fromJson(response), function (row) {
                if (map[row["employee_id"]] === undefined) {
                    ++selectSize;
                    dojo.create("option", { value: row["employee_id"], innerHTML: row["employee_name"]}, employeeSelect);
                }
            });

            if (selectSize == 0) {
                dojo.create("option", { value: "-1", innerHTML: "Сотрудников не найдено", disabled: true}, employeeSelect);
                return;
            }
        }
    };
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
