var month = new Array();

month[0] = "Январь";
month[1] = "Февраль";
month[2] = "Март";
month[3] = "Апрель";
month[4] = "Май";
month[5] = "Июнь";
month[6] = "Июль";
month[7] = "Август";
month[8] = "Сентябрь";
month[9] = "Октябрь";
month[10]= "Ноябрь";
month[11]= "Декабрь";

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

function monthCount(yearStart, monthStart, yearEnd, monthEnd){
    var cnt = 0;
    iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function(){++cnt});
    return cnt;
}


function textFormat(value, color){
    if (color === undefined){
        color = 'gray';
    }

    if (value === undefined || value == null){
        value = "0";
    }

    if (isNumber(value)){
        value = Math.round(value);
        if (value > 100) {
            color = "#E32636";
        } else if (value > 0){
            color = "black";
        }
    }

    return dojo.create(
        "span",
        {
            innerHTML:value + '%',
            style:"color: " + color + "; font-weight: bold"
        }
    ).outerHTML
}

// Формат вывода ячеек в гриде
function formatterData(value){
    return textFormat(value);
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

    return textFormat(showValue, color);
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
            handler(response, employeeId, yearStart, monthStart, yearEnd, monthEnd);
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

// Делает ajax запрос, возвращающий проекты по центру,
// полученны ответ в виде JSON передает в функцию handler(json_value)
function additionProjectDataHandler(division, monthBegin, yearBegin, handler){
    dojo.xhrGet({
        url: "/employmentPlanning/getProjectByDivisionAsJSON",
        content: {
            divisionId: division,
            monthBegin: monthBegin,
            yearBegin: yearBegin
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

// Делает ajax запрос, для сохранения планируемого процента занятости
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
            alert('saveEmployeeDataHandler Panic !');
        }
    });
}


// Делает ajax запрос, для сохранения планируемого процента занятости
function saveProjectDataHandler(jsonData, employeeId, handler){
    dojo.xhrPost({
        url: "/employmentPlanning/setProjectDataAsJSON",
        content: {
             jsonData: jsonData,
             employeeId: employeeId
        },
        handleAs: "text",
        load: function(response, ioArgs) {
            handler(response);
        },
        error: function(response, ioArgs) {
            alert('saveProjectDataHandler Panic !');
        }
    });
}

// Переводит объект в JSON
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
    return data;
}