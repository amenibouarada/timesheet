dojo.require("dojox.widget.Standby");
var standByElement;
dojo.ready(function () {
    //крутилка создается при после загрузки страницы,
    //т.к. если она создается в месте использования - ghb show не отображается картинка
    standByElement = new dojox.widget.Standby({target: dojo.query("body")[0], zIndex: 1000});
});

function showErrors(/* Array */ errors) {
    var errorsStr = '';

    dojo.forEach(errors, function (error) {
        errorsStr += error + "\n\n";
    });

    if (errorsStr.length == 0) {
        return false;
    }

    alert(errorsStr);
    return true;
}

function handleError(message) {
    console.log(message);
}

// Возвращает название месяца по номеру
function getMonthByNumber(number) {
    var month = ["Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль",
        "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"];
    return month[number - 1];
}

// Бежит по циклу между двумя месяцами и для каждого месяца дергает функцию handler(month, year)
function iterateMonth(yearStart, monthStart, yearEnd, monthEnd, handler) {
    for (var month = monthStart, year = yearStart; (month <= monthEnd && year == yearEnd) || year < yearEnd;) {
        handler(month, year);
        ++month;
        if (month == 13) {
            month = 1;
            ++year;
        }
    }
}

function monthCount(yearStart, monthStart, yearEnd, monthEnd) {
    var cnt = 0;
    iterateMonth(yearStart, monthStart, yearEnd, monthEnd, function () {
        ++cnt
    });
    return cnt;
}

function isNumber(n) {
    return (typeof n != typeof undefined) && !isNaN(parseFloat(n)) && isFinite(n);
}

function isNilOrNull(obj) {
    return !(obj != null && obj != 0);
}

function isUndefinedNullNaN(value) {
    return value == NaN || value == null || value == undefined;
}

function showTooltip(obj) {
    tooltip.show(getTitle(obj));
}

function hideTooltip(obj) {
    tooltip.hide();
}


/* объект подсказки */
var tooltip = function () {
    var id = 'tt';
    var top = 3;
    var left = 3;
    var maxw = 300;
    var speed = 10;
    var timer = 20;
    var endalpha = 95;
    var alpha = 0;
    var tt, t, c, b, h;
    var ie = document.all ? true : false;
    return{
        show: function (v, w) {
            if (tt == null) {
                tt = document.createElement('div');
                tt.setAttribute('id', id);
                t = document.createElement('div');
                t.setAttribute('id', id + 'top');
                c = document.createElement('div');
                c.setAttribute('id', id + 'cont');
                b = document.createElement('div');
                b.setAttribute('id', id + 'bot');
                tt.appendChild(t);
                tt.appendChild(c);
                tt.appendChild(b);
                document.body.appendChild(tt);
                tt.style.opacity = 0;
                tt.style.filter = 'alpha(opacity=0)';
                document.onmousemove = this.pos;
            }
            tt.style.display = 'block';
            c.innerHTML = v;
            tt.style.width = w ? w + 'px' : 'auto';
            if (!w && ie) {
                t.style.display = 'none';
                b.style.display = 'none';
                tt.style.width = tt.offsetWidth;
                t.style.display = 'block';
                b.style.display = 'block';
            }
            if (tt.offsetWidth > maxw) {
                tt.style.width = maxw + 'px'
            }
            h = parseInt(tt.offsetHeight) + top;
            clearInterval(tt.timer);
            tt.timer = setInterval(function () {
                tooltip.fade(1)
            }, timer);
        },
        pos: function (e) {
            var u = ie ? event.clientY + document.documentElement.scrollTop : e.pageY;
            var l = ie ? event.clientX + document.documentElement.scrollLeft : e.pageX;
            tt.style.top = (u - h) + 'px';
            tt.style.left = (l + left) + 'px';
        },
        fade: function (d) {
            var a = alpha;
            if ((a != endalpha && d == 1) || (a != 0 && d == -1)) {
                var i = speed;
                if (endalpha - a < speed && d == 1) {
                    i = endalpha - a;
                } else if (alpha < speed && d == -1) {
                    i = a;
                }
                alpha = a + (i * d);
                tt.style.opacity = alpha * .01;
                tt.style.filter = 'alpha(opacity=' + alpha + ')';
            } else {
                clearInterval(tt.timer);
                if (d == -1) {
                    tt.style.display = 'none'
                }
            }
        },
        hide: function () {
            clearInterval(tt.timer);
            tt.timer = setInterval(function () {
                tooltip.fade(-1)
            }, timer);
        }
    };
}();

//необходимо переопределить на своих страницах
function getEmployeeData() {
    throw "getEmployeeData is unsupported method"
}

// возвращает первый рабочий день сотрудника
function getFirstWorkDate() {
    var employeeId = dojo.byId("employeeId").value;
    var employee = getEmployeeData();
    return convertStringToDate(employee.firstWorkDate);
}

/* Создает cookie с указанными параметрами */
function setCookie(name, value, exp, pth, dmn, sec) {
    document.cookie = name + '=' + escape(value)
        + ((exp) ? '; expires=' + exp : '')
        + ((pth) ? '; path=' + pth : '')
        + ((dmn) ? '; domain=' + dmn : '')
        + ((sec) ? '; secure' : '');
}

/* Выдает время через d дней h часов m минут */
function TimeAfter(d, h, m) {
    var now = new Date(), // объект класса Data
        nowMS = now.getTime(), // в миллисекундах (мс)
        newMS = ((d * 24 + h) * 60 + m) * 60 * 1000 + nowMS;
    now.setTime(newMS);    // новое время в мс
    return now.toGMTString();
}

/**
 * Заполняет список доступных проектов/пресейлов
 * @param rowIndex
 * @param projectState тип проекта (проект, пресейл ...)
 */
function fillProjectList(rowIndex, projectState) {
    var projectSelect = dojo.byId("project_id_" + rowIndex);
    var division = dojo.byId("divisionId").value;
    if (division != 0) {

        dojo.forEach(dojo.filter(projectList, function (projectsOfDiv) {
            return (projectsOfDiv.divId == division);
        }), function (projectsOfDiv) {
            dojo.forEach(dojo.filter(projectsOfDiv.divProjs, function (project) {
                return ((+project.state === projectState) && (project.active == 'true'));
            }), function (project) {
                var projectOption = dojo.doc.createElement("option");
                dojo.attr(projectOption, {
                    value: project.id
                });
                projectOption.title = project.value;
                projectOption.innerHTML = project.value;
                projectSelect.appendChild(projectOption);
            });
        });

        if (existsCookie('aplanaProject')) {
            projectSelect.value = getCookieValue('aplanaProject');
            projectChange(projectSelect);
        }
    } else {

    }
    sortSelectOptions(projectSelect);
}

/* Добавляет в указанный select пустой option. */
function insertEmptyOption(select) {
    insertEmptyOptionWithCaptionInHead(select, "");
}

/* Добавляет в указанный select пустой option с указанной подписью. */
function insertEmptyOptionWithCaptionInHead(select, caption, value) {
    value = value || "0"; // если не указан, то по умолчанию 0
    var option = dojo.doc.createElement("option");
    dojo.attr(option, {
        value: value
    });
    option.innerHTML = caption;
    select.insertBefore(option, select.options[0]);
}

// Снимает выбранные элементы в селекте
function unselectValues(select) {
    for (var option in select.selectedOptions){
        option.selected = false;
    }
}

// Возвращает все выбранные значение(value) в <select multiple="true">
function getSelectValues(select) {
    var result = [];
    for (var option in select.selectedOptions){
        result.push(option.value);
    }
    return result;
}

/* Выставляет должность сотрудника (проектная роль по умолчанию) */
function setDefaultEmployeeJob(rowIndex) {
    if (!document.employeeList || !dojo.byId("divisionId")) {
        return;
    }
    var selectedEmployeeId = dojo.byId("employeeId").value;
    var divisionId = dojo.byId("divisionId").value;
    var defaultEmployeeJobId = 0;
    for (var i = 0; i < employeeList.length; i++) {
        if (divisionId == employeeList[i].divId) {
            for (var j = 0; j < employeeList[i].divEmps.length; j++) {
                if (employeeList[i].divEmps[j].id == selectedEmployeeId) {
                    defaultEmployeeJobId = employeeList[i].divEmps[j].jobId;
                    break;
                }
            }
        }
    }
    var actTypeLists = new Array();
    if (rowIndex >= 0) {
        actTypeLists.push(dojo.byId("activity_type_id_" + rowIndex));
    } else { //Если функция вызвана при выборе сотрудника
        actTypeLists = dojo.query(".activityType");
    }

    for (var j = 0; j < actTypeLists.length; j++) {
        var listId = dojo.attr(actTypeLists[j], "id");
        var row = listId.substring(listId.lastIndexOf("_") + 1, listId.length);
        // Проект Пресейл
        var projectRoleIdText = "project_role_id_" + row;
        if ((actTypeLists[j].value == EnumConstants.TypesOfActivityEnum.PROJECT)
            || (actTypeLists[j].value == EnumConstants.TypesOfActivityEnum.PRESALE)) {
            var projectRoleList = dojo.byId(projectRoleIdText);
            dojo.attr(projectRoleList, { value: defaultEmployeeJobId });
            fillAvailableActivityCategoryList(row);
        }
        // Внепроектная активность
        else if (actTypeLists[j].value == "14") {
            var projectRoleList = dojo.byId(projectRoleIdText);
            dojo.attr(projectRoleList, { value: defaultEmployeeJobId });
            fillAvailableActivityCategoryList(row);
        }
    }
}

/*
 * функция чтобы показать хинт для уже выбранных значений селектов
 * здесь title-это атрибут у селекта - он же хинт
 */
function getTitle(processed) {
    var select = null;
    if (processed.target == null) {
        select = processed;
    }
    else {
        select = processed.target;
    }
    //костыль чтобы в категории активности отображалось описание
    if (select.id.indexOf("activity_category_id_") + 1) {
        var description = dojo.byId("act_description_" + select.id.substring(21)).innerHTML;
        if (description && description.trim() != "") {
            return  description;
        }
    }
    //
    var index = select.selectedIndex;
    if (select.options != null) {
        if ((index > -1) && (select.options[index].text != "")) {
            return select.options[index].text;
        }
        else {
            return 'значение еще не выбрано';
        }
    }
    else if (select.textbox != null) {
        if (select.textbox.value != "") return select.textbox.value
        else return 'значение еще не выбрано';
    }
}

/* Сортирует по алфавиту содержимое выпадающих списков. */
function sortSelectOptions(select) {
    var tmpArray = [];
    for (var i = 0; i < select.options.length; i++) {
        tmpArray.push(select.options[i]);
    }
    tmpArray.sort(function (a, b) {
        return (a.text < b.text) ? -1 : 1;
    });
    select.options.length = 0;
    for (var i = 0; i < tmpArray.length; i++) {
        select.options[i] = tmpArray[i];
    }
}

function openViewReportsWindow() {
    var employeeId = dojo.byId("employeeId").value;
    if (employeeId != 0) {
        var date = new Date();
        window.open('viewreports/' + employeeId + '/' + date.getFullYear() + '/' + (date.getMonth() + 1), 'reports_window' + employeeId);
    }
}

function getRootEventListener() {
    return window.addEventListener || window.attachEvent ? window : document.addEventListener ? document : null;
}

function clearErrorBox(divId) {
    var target = dojo.byId(divId);
    if (target != null) {
        dojo.destroy(divId);
    }
}

// ковертирует дату в тип дата из строки (разделитель ".")
function convertStringToDate(stringDate) {
    if (stringDate != null && stringDate != "") {
        var date = stringDate.split('.');
        return new Date(date[2], date[1] - 1, date[0]);
    } else {
        return null;
    }
}

/*
 Запускаем Standby widget "крутилка" на весь экран
  */
function processing() {
    document.body.appendChild(standByElement.domNode);
    standByElement.startup();
    standByElement.show();
}

function stopProcessing() {
    standByElement.hide();
}

/**
 * Обновляет список руководителей подразделения
 *
 * @param currentValue      - текущее значение селекта руководителей, чтоб после обновления попробовать его же и установить
 * @param managerList       - список всех руководителей типа List<Employee>
 * @param divisionId        - подразделение, по которому определяется список сотрудников
 * @param managerSelect     - селект, который заполняется руководителями
 */
function updateManagerListByDivision(currentValue, managerList, divisionId, managerSelect) {
    var optionAllValue = -1;
    // зададим значения по умолчанию
    managerList = managerList || managerMapJson;
    divisionId = divisionId || dojo.byId("divisionId").value;
    managerSelect = managerSelect || dojo.byId("managerId");
    currentValue = managerSelect.value || optionAllValue;

    managerSelect.options.length = 0;
    insertEmptyOptionWithCaptionInHead(managerSelect, "Все руководители", optionAllValue);

    if (managerList.length > 0) {
        dojo.forEach(dojo.filter(managerList, function (m) {
            return (m.division == divisionId);
        }), function (managerData) {
            var option = document.createElement("option");
            dojo.attr(option, {
                value: managerData.id
            });
            option.title = managerData.name;
            option.innerHTML = managerData.name;
            managerSelect.appendChild(option);
        });
    }
    if (managerSelect.options.length == 1 && managerSelect.options[0].value == optionAllValue) {
        managerSelect.disabled = true;
    } else {
        managerSelect.disabled = '';
    }
    managerSelect.value = currentValue;
    if (managerSelect.value == "") {
        managerSelect.value = optionAllValue;
    }
}

/* Удаляет куки с данным именем */
function deleteCookie(CookieName) {
    setCookie(CookieName, '', TimeAfter(-1, 0, 0));
}

/* Узнает, имеется ли куки с данным именем */
function existsCookie(CookieName) {
    return (document.cookie.split(CookieName + '=').length > 1);
}

/* Выдает значение куки с данным именем */
function getCookieValue(CookieName) {
    var razrez = document.cookie.split(CookieName + '=');
    if (razrez.length > 1) { // Значит, куки с этим именем существует
        var hvost = razrez[1],
            tzpt = hvost.indexOf(';'),
            EndOfValue = (tzpt > -1) ? tzpt : hvost.length;
        return unescape(hvost.substring(0, EndOfValue));
    }
}
