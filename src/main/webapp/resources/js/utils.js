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

function handleError(message){
    console.log(message);
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
        show:function (v, w) {
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
        pos:function (e) {
            var u = ie ? event.clientY + document.documentElement.scrollTop : e.pageY;
            var l = ie ? event.clientX + document.documentElement.scrollLeft : e.pageX;
            tt.style.top = (u - h) + 'px';
            tt.style.left = (l + left) + 'px';
        },
        fade:function (d) {
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
        hide:function () {
            clearInterval(tt.timer);
            tt.timer = setInterval(function () {
                tooltip.fade(-1)
            }, timer);
        }
    };
}();

//необходимо переопределить на своих страницах
function getEmployeeData(){
    throw "getEmployeeData is unsupported method"
}

// возвращает первый рабочий день сотрудника
function getFirstWorkDate(){
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

/* Заполняет список доступных проектов/пресейлов */
function fillProjectList(rowIndex, projectState) {
    var projectSelect = dojo.byId("project_id_" + rowIndex);
    var division = dojo.byId("divisionId").value;
    if (division != 0) {
        for (var i = 0; i < projectList.length; i++) {
            if (division == projectList[i].divId) {
                projectSelect.options.length = 0;
                insertEmptyOption(projectSelect);
                for (var j = 0; j < projectList[i].divProjs.length; j++) {
                    if ((projectList[i].divProjs[j].state == projectState) && (projectList[i].divProjs[j].active == 'true')) {
                        projectOption = dojo.doc.createElement("option");
                        dojo.attr(projectOption, {
                            value:projectList[i].divProjs[j].id
                        });
                        projectOption.title = projectList[i].divProjs[j].value;
                        projectOption.innerHTML = projectList[i].divProjs[j].value;
                        projectSelect.appendChild(projectOption);
                    }
                }
            }
        }
        if (existsCookie('aplanaProject')) {
            projectSelect.value = cookieValue('aplanaProject');
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
function insertEmptyOptionWithCaptionInHead(select, caption) {
    var option = dojo.doc.createElement("option");
    dojo.attr(option, {
        value:"0"
    });
    option.innerHTML = caption;
    select.insertBefore(option, select.options[0]);
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
        if (    (actTypeLists[j].value == EnumConstants.TypesOfActivityEnum.PROJECT)
            ||  (actTypeLists[j].value == EnumConstants.TypesOfActivityEnum.PRESALE)) {
            var projectRoleList = dojo.byId(projectRoleIdText);
            dojo.attr(projectRoleList, { value:defaultEmployeeJobId });
            fillAvailableActivityCategoryList(row);
        }
        // Внепроектная активность
        else if (actTypeLists[j].value == "14") {
            var projectRoleList = dojo.byId(projectRoleIdText);
            dojo.attr(projectRoleList, { value:defaultEmployeeJobId });
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
    if(select.id.indexOf("activity_category_id_")+1){
        var description = dojo.byId("act_description_" + select.id.substring(21)).innerHTML;
        if(description && description.trim()!=""){
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
    var divisionId = dojo.byId("divisionId").value;
    if (employeeId != 0) {
        var date = new Date();
        window.open('viewreports/' + divisionId + '/' + employeeId + '/' + date.getFullYear() + '/' + (date.getMonth() + 1), 'reports_window' + employeeId);
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
function convertStringToDate(stringDate){
    if (stringDate != null && stringDate != "") {
        var date = stringDate.split('.');
        return new Date(date[2], date[1]-1, date[0]);
    }else{
        return null;
    }
}

