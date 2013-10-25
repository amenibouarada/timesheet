var month = new Array();

month[0] = "January";
month[1] = "February";
month[2] = "March";
month[3] = "April";
month[4] = "May";
month[5] = "June";
month[6] = "July";
month[7] = "August";
month[8] = "September";
month[9] = "October";
month[10]= "November";
month[11]= "December";

// Число ли это
function isNumber(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
}

// Возвращает название месяца по номеру
function getMonthByNumber(number){
    return month[number - 1];
}

// Возвращает все выбранные значение(value) в <select multiple="true">
function getSelectValues(select) {
    var result = [];
    var options = select && select.options;
    var opt;

    for (var i=0, iLen=options.length; i<iLen; i++) {
        opt = options[i];

        if (opt.selected) {
            result.push(opt.value);
        }
    }
    return result;
}


// Возвращает все выбранные значение(в виде: [value, text]) в <select multiple="true">
function getSelectObjects(select) {
    var result = [];
    var options = select && select.options;
    var opt;

    for (var i=0, iLen=options.length; i<iLen; i++) {
        opt = options[i];

        if (opt.selected) {
            result.push(opt);
        }
    }
    return result;
}

// Очищает все значения в select
function clearSelectValues(select){
    var i=0;
    for(;i<select.length;){
        select.options[i]=null;
    }
}

// Снимает выбранные элементы в селекте
function unselectValues(select){
    for(var i=0; i<select.length;++i){
        select.options[i].selected=false;
    }
}

// Бежит по циклу между двумя месяцами и для каждого месяца дергает функцию handler(month, year)
function iterateMonth(yearStart, monthStart, yearEnd, monthEnd, handler){
    for (var month = monthStart, year = yearStart; (month <= monthEnd && year == yearEnd) || year < yearEnd;){
        handler(month, year);

        ++month;
        if (month == 13){
            month = 1;
            ++year;
        }
    }
}

// Формат вывода ячеек в гриде
function formatterData(value){
    var color = "gray";
    var showValue;

    if (value === undefined){
        showValue = "0";
    } else {
        if (isNumber(value)){
            showValue = Math.round(value);
            if (value > 100) {
                color = "red";
            } else {
                color = "black";
            }
        } else {
            showValue = value;
        }
    }

    return dojo.create(
        "span",
        {
            innerHTML:showValue +'%',
            style:"color: " + color + "; font-weight: bold"
        }
    ).outerHTML;
}

// Формат вывода ячеек в гриде для редактируемых полей
function formatterEditableData(value){
    var color = "black";
    var showValue;

    if ((value === undefined) || (!isNumber(value))){
        return;
    } else {
        showValue = Math.round(value);
        if (value > 100) {
            color = "red";
        }
    }

    return dojo.create(
        "span",
        {
            innerHTML:showValue,
            style:"color: " + color + "; font-weight: bold"
        }
    ).outerHTML;
}
// Делает ajax запрос по занятости сотрудников на проекте, полученный json ответ отправляет в функцию handler(json_value)
function projectDataHandler(projectId, yearStart, monthStart, yearEnd, monthEnd, handler){
    dojo.xhrGet({
        url: "/employmentPlanning/getProjectPlanAsJSON",
        content: {
            yearBeg: yearStart,
            monthBeg: monthStart,
            yearEnd: yearEnd,
            monthEnd: monthEnd,
            projectId: projectId
        },
        handleAs: "text",
        load: function(response, ioArgs) {
            handler(response, yearStart, monthStart, yearEnd, monthEnd);
        },
        error: function(response, ioArgs) {
            alert('projectDataHandler Panic !');
        }
    });
}

// Делает ajax запрос по занятости сотрудника на проектах, полученный json ответ отправляет в функцию handler(json_value)
function employeeDataHandler(employeeId, yearStart, monthStart, yearEnd, monthEnd, handler){
    dojo.xhrGet({
        url: "/employmentPlanning/getEmployeePlanAsJSON",
        content: {
            yearBeg: yearStart,
            monthBeg: monthStart,
            yearEnd: yearEnd,
            monthEnd: monthEnd,
            employeeId: employeeId
        },
        handleAs: "text",
        load: function(response, ioArgs) {
            handler(response, yearStart, monthStart, yearEnd, monthEnd);
        },
        error: function(response, ioArgs) {
            alert('employeeDataHandler Panic !');
        }
    });
}

// Делает ajax запрос, возвращающий сотрудников по центру/руководителю/должности/региону,
// полученны ответ в виде JSON передает в функцию handler(json_value)
function additionEmployeeDataHandler(division, manager, roleList, regionList, handler){
    dojo.xhrGet({
        url: "/employmentPlanning/getAddEmployeeListAsJSON",
        content: {
            divisionId: division,
            managerId: manager,
            projectRoleListId: roleList,
            regionListId: regionList
        },
        handleAs: "text",
        load: function(response, ioArgs) {
            handler(response);
        },
        error: function(response, ioArgs) {
            alert('additionEmployeeDataHandler Panic !');
        }
    });
}

/**
 * Сохранение данных
 */
function saveEmployeeDataHandler(projectId, monthBeg, yearBeg, monthEnd, yearEnd, jsonData, handler){
    dojo.xhrPost({
        url: "/employmentPlanning/setEmployeeProjectAsJSON",
        content: {
            projectId: projectId,
            monthBeg: monthBeg,
            yearBeg: yearBeg,
            monthEnd: monthEnd,
            yearEnd: yearEnd,
            jsonData: jsonData
        },
        handleAs: "text",
        load: function(response, ioArgs) {
            handler(response);
        },
        error: function(response, ioArgs) {
            alert('additionEmployeeDataHandler Panic !');
        }
    });
}

function itemToJSON(store, items){
    var data = [];
    if(items && store){
        for(var n=0; n<items.length; ++n){
            var json = {};
            var item = items[n];
            if (item){
                // Determine the attributes we need to process.
                var attributes = store.getAttributes(item);
                if(attributes && attributes.length > 0){
                    for(var i = 0; i < attributes.length; i++){
                        var values = store.getValues(item, attributes[i]);
                        if(values){
                            // Handle multivalued and single-valued attributes.
                            if(values.length > 1 ){
                                json[attributes[i]] = [];
                                for(var j = 0; j < values.length; j++ ){
                                    var value = values[j];
                                    // Check that the value isn't another item. If it is, process it as an item.
                                    if(store.isItem(value)){
                                        json[attributes[i]].push(dojo.fromJson(itemToJSON(store, value)));
                                    }else{
                                        json[attributes[i]].push(value);
                                    }
                                }
                            }else{
                                if(store.isItem(values[0])){
                                    json[attributes[i]] = dojo.fromJson(itemToJSON(store, values[0]));
                                }else{
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
    return '{"employee": [' + data + ']}';
}