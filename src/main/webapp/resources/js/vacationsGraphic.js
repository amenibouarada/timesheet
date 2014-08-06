var tableRowHeightKoef = 20;
var tableColumnWidthKoef = 20;
var tableColumnWidthForEmployeeName = 200;
var leftIndent = tableColumnWidthForEmployeeName + 2;
var tableRowCount = 0;
var dayCountToAdd = 5;
var tableHeaderHeight = 31;
var topAfterHeader = tableHeaderHeight + 7;
function getTableHeight(){
    return tableRowCount * tableRowHeightKoef + 10;
}


function RegionEmployees(regionName, regionEmployees, holidayList){
    var employees = new Array();
    for (var i in regionEmployees){
        var employeeName = regionEmployees[i].employee;
        var employeeVacations = regionEmployees[i].vacations;
        var vacations = new EmployeeVacations(employeeName, employeeVacations);
        employees.push(vacations);
    }

    this.getRegion   = function(){ return regionName};
    this.getEmployees = function(){ return employees};
    this.getHoliday = function() {return holidayList};
}

function EmployeeVacations(employeeName, employeeVacations){
    var employee = employeeName;
    var vacations = new Array();
    for (var i in employeeVacations){
        employeeVacations[i].beginDate = getDateByString(employeeVacations[i].beginDate);
        employeeVacations[i].endDate = getDateByString(employeeVacations[i].endDate);
        vacations.push(employeeVacations[i]);
    }

    this.getEmployee  = function(){ return employee};
    this.getVacations = function(){ return vacations};
}

function Gantt(gDiv, holidayList, type, sDate, eDate)
{
    var GanttDiv = gDiv;
    var viewType = type;
    if (viewType == VIEW_GRAPHIC_BY_WEEK){
        tableColumnWidthKoef = 10;
    }else{
        tableColumnWidthKoef = 20;
    }

    var startDate = getDateByString(sDate); // дата "С" с формы
    var endDate = getDateByString(eDate);   // дата "ПО" с формы

    var holidays = new Array();
    for (var i in holidayList){
        var date = getDateByString(holidayList[i]);
        holidays.push(date.toLocaleDateString());
    }

    var regionEmployeeList = new Array();
    this.AddRegionEmployeeList = function(value){
        regionEmployeeList.push(value);
    }

    this.Draw = function(){
        var offSet = 0;
        var dateDiff = 0;
        var gStr = "";

        if(regionEmployeeList.length <= 0){
            return
        }

        var vacations = createFullVacationsList(regionEmployeeList);
        var maxDate = findMaxDate(vacations, viewType, endDate);
        var minDate = findMinDate(vacations, viewType, startDate);

        tableRowCount = getTableRowCount(regionEmployeeList);

        gStr = fillTableHeader(viewType, minDate, maxDate, holidays, tableRowCount);
        var employeeCount = 0;
        var rowHeight = 0;
        for (var i in regionEmployeeList){
            var employeeList = sortEmployeeList(regionEmployeeList[i].getEmployees());
            var indent = parseInt(i) + parseInt(employeeCount);
            var topLine = tableRowHeightKoef * indent + topAfterHeader;
            gStr += "<div style='position:absolute; top:" + topLine + "px; left:5px'><b>" + regionEmployeeList[i].getRegion() + "</b></div>";
            employeeCount += employeeList.length;
            for (var hol in regionEmployeeList[i].getHoliday()) {
                var holDate = getDateByString(regionEmployeeList[i].getHoliday()[hol]);
                var leftOffset = dateDiffInDay(minDate, holDate) * tableColumnWidthKoef + leftIndent;
                rowHeight = (employeeList.length + 1) * tableRowHeightKoef + 6; // прибавим 6, чтобы расстянуть до конца таблицы
                gStr += "<div style='position:absolute; top:" + topLine + "px; left:" + leftOffset + "px; width:" + tableColumnWidthKoef + "px'><div title='" + holDate.toLocaleDateString() + "\nРегиональный праздник' class='GRegionHoliday' style='width:" + tableColumnWidthKoef + "px;height:" + rowHeight + "px;'></div></div>";
            }
            for (var j in employeeList){
                var vacationList = sortVacationsByType(employeeList[j].getVacations());
                indent += 1;
                topLine = tableRowHeightKoef * indent + topAfterHeader;
                for(var k in vacationList){
                    var color = getColorByType(vacationList[k].type);
                    var tooltip = employeeList[j].getEmployee() + "\n" + vacationList[k].typeName + "\n" + vacationList[k].beginDate.toLocaleDateString() + " - " + vacationList[k].endDate.toLocaleDateString() + "\n" + vacationList[k].status ;
                    offSet = dateDiffInDay(minDate, vacationList[k].beginDate);
                    dateDiff = dateDiffInDay(vacationList[k].beginDate, vacationList[k].endDate) + 1;
                    gStr += "<div style='position:absolute; top:" + topLine + "px; left:" + (offSet * tableColumnWidthKoef + leftIndent) + "px; width:" + (tableColumnWidthKoef * dateDiff - 1) + "px'><div title='" + tooltip + "' class='GVacation' style='background-color:" + color + "; width:" + (tableColumnWidthKoef * dateDiff - 1) + "px;'>" + "</div></div>";
                }
                gStr += "<div style='position:absolute; top:" + topLine + "px; left:5px'>" +  employeeList[j].getEmployee() + "</div>";
            }
        }

        gStr += drawUnaskedPeriod(startDate, endDate, minDate, maxDate);

        GanttDiv.innerHTML = gStr;
    }
}

// рисует полупрозрачную область над тем периодом, который не запрашивался, но в этом периоде есть отпуска
function drawUnaskedPeriod(startDate, endDate, minDate, maxDate){
    var dateDiff = dateDiffInDay(minDate, startDate);
    var leftBlock = "<div class='GUnAskedPeriod' style='position:absolute; top:33px; left:" + leftIndent + "px; width:" + (tableColumnWidthKoef * dateDiff - 1) + "px; height:" + getTableHeight() + "px'></div>";
    dateDiff = dateDiffInDay(endDate, maxDate);
    var beginDiff = (dateDiffInDay(minDate, endDate) + 1) * tableColumnWidthKoef + leftIndent;
    var rightBlock = "<div class='GUnAskedPeriod' style='position:absolute; top:33px; left:" + beginDiff + "px; width:" + (tableColumnWidthKoef * dateDiff - 1) + "px; height:" + getTableHeight() + "px'></div>";
    return leftBlock + rightBlock;
}

function dateDiffInDay(begin, end){
    return (Date.parse(end) - Date.parse(begin)) / 86400000; // разница в датах в днях
}

function getDateByString(dateString){
    if (dateString instanceof Date){ return dateString; }
    var date = new Date();
    var dvArr = dateString.split('.');
    date.setFullYear(dvArr[2], dvArr[1] - 1, dvArr[0]);
    return date;
}

function sortEmployeeList(employeeList){
    for (var i = 0; i < employeeList.length; i++){
        for (var j = i + 1; j < employeeList.length; j++){
            if (employeeList[i].getEmployee() > employeeList[j].getEmployee()){
                var tmp = employeeList[i];
                employeeList[i] = employeeList[j];
                employeeList[j] = tmp;
            }
        }
    }
    return employeeList;
}

// эта сортировка нужна для того, чтобы реальные отпуска перекрывали планируемые
function sortVacationsByType(vacations){
    var arrayWithPlanVacations = new Array();
    var arrayWithRealVacations = new Array();
    for (var i in vacations){
        if (vacations[i].type == VACATION_PLANNED) {// планируемый отпуск
            arrayWithPlanVacations.push(vacations[i]);
        }else{
            arrayWithRealVacations.push(vacations[i]);
        }
    }
    arrayWithPlanVacations = sortVacationsByDate(arrayWithPlanVacations);
    arrayWithRealVacations = sortVacationsByDate(arrayWithRealVacations);
    return arrayWithPlanVacations.concat(arrayWithRealVacations);
}

function sortVacationsByDate(vacations){
    for (var i = 0; i < vacations.length; i++){
        for (var j = i + 1; j < vacations.length; j++){
            if (vacations[i].beginDate > vacations[j].beginDate){
                var tmp = vacations[i];
                vacations[i] = vacations[j];
                vacations[j] = tmp;
            }
        }
    }
    return vacations;
}

function getColorByType(type){
    switch (type){
        case VACATION_WITH_PAY    : return "green";    // отпуск с сохранением содержания
        case VACATION_WITHOUT_PAY : return "green";    // отпуск без сохранения содержания
        case VACATION_WITH_WORK   : return "green";    // отпуск с последующей отработкой
        case VACATION_PLANNED     : return "yellow";   // планируемый отпуск
    }
}

function createFullVacationsList(regionEmployeeList){
    var vacations = new Array();
    for (var i in regionEmployeeList){
        var employeeList = regionEmployeeList[i].getEmployees();
        for (var j in employeeList){
            var employeeVacations = employeeList[j].getVacations();
            for (var k in employeeVacations){
                vacations.push(employeeVacations[k]);
            }
        }
    }
    return vacations;
}

function getTableRowCount(regionEmployeeList){
    var result = regionEmployeeList.length;
    for (var i in regionEmployeeList){
        result += regionEmployeeList[i].getEmployees().length;
    }
    return result;
}

function getLastDayOfMonth(year, month) {
    var date = new Date(year, month+1, 0);
    return date.getDate();
}

function findMaxDate(vacations, viewType, endDate){
    var maxDate = new Date();
    maxDate.setFullYear(vacations[0].endDate.getFullYear(),
                        vacations[0].endDate.getMonth(),
                        vacations[0].endDate.getDate());
    // найдем максимальную дату
    for (var i in vacations){
        if(Date.parse(vacations[i].endDate) > Date.parse(maxDate)){
            maxDate.setFullYear(vacations[i].endDate.getFullYear(), vacations[i].endDate.getMonth(), vacations[i].endDate.getDate());
        }
    }
    if (endDate > maxDate){
        maxDate = new Date(endDate.valueOf());
    }
    //---- Добавим несколько дней к maxDate, если нужно, чтобы поместилось название месяца -----
    var day = maxDate.getDate() - dayCountToAdd;
    if (day < 0){
        maxDate.setDate(maxDate.getDate() + Math.abs(day) + 1);
    }

    if (viewType == VIEW_GRAPHIC_BY_WEEK){
        maxDate.setDate(maxDate.getDate() + dayCountToAdd);
        while(maxDate.getDay() != 0){
            maxDate.setDate(maxDate.getDate() + 1);
        }
    }
    return maxDate;
}

function findMinDate(vacations, viewType, startDate){
    var minDate = new Date();
    minDate.setFullYear(vacations[0].beginDate.getFullYear(),
                        vacations[0].beginDate.getMonth(),
                        vacations[0].beginDate.getDate());
    // найдем минимальную дату
    for (var i in vacations){
        if(Date.parse(vacations[i].beginDate) < Date.parse(minDate)){
            minDate.setFullYear(vacations[i].beginDate.getFullYear(), vacations[i].beginDate.getMonth(), vacations[i].beginDate.getDate());
        }
    }
    if (startDate < minDate){
        minDate = new Date(startDate.valueOf());
    }
    //---- Добавим несколько дней к maxDate, если нужно, чтобы поместилось название месяца -----
    var day = getLastDayOfMonth(minDate.getFullYear(), minDate.getMonth()) - minDate.getDate();
    if (day < dayCountToAdd){
        minDate.setDate(minDate.getDate() - Math.abs(dayCountToAdd - day));
    }

    if (viewType == VIEW_GRAPHIC_BY_WEEK){
        while (minDate.getDay() != 1){ // пока не понедельник
            minDate.setDate(minDate.getDate() - 1);
        }
    }
    return minDate;
}

// GET NUMBER OF DAYS IN MONTH
function getDaysInMonth(month, year)
{
    var days;
    switch(month)
    {
        case 1:
        case 3:
        case 5:
        case 7:
        case 8:
        case 10:
        case 12:
            days = 31;
            break;
        case 4:
        case 6:
        case 9:
        case 11:
            days = 30;
            break;
        case 2:
            if (((year% 4)==0) && ((year% 100)!=0) || ((year% 400)==0))
                days = 29;
            else
                days = 28;
            break;
    }
    return (days);
}

function getMonthByNumber(number){
    var month;
    switch(number)
    {
        case 1:  month = 'январь'; break;
        case 2:  month = 'февраль'; break;
        case 3:  month = 'март'; break;
        case 4:  month = 'апрель'; break;
        case 5:  month = 'май'; break;
        case 6:  month = 'июнь'; break;
        case 7:  month = 'июль'; break;
        case 8:  month = 'август'; break;
        case 9:  month = 'сентябрь'; break;
        case 10: month = 'октябрь'; break;
        case 11: month = 'ноябрь'; break;
        case 12: month = 'декабрь'; break;
    }
    return (month);
}

function getWeek(date){ // возвращает номер недели
    var tmp = new Date(date.getFullYear(),0,1);
    return Math.ceil(((dateDiffInDay(tmp, date)) + tmp.getDay() + 1)/7);
}

function addWeek(date){
    var tmpDate = new Date(date);
    tmpDate.setDate(tmpDate.getDate() + 7);
    return tmpDate;
}

function makeMonthDivTitle(date){
    var firstDay = new Date(date);
    var lastDay  = new Date(date);
    var month;
    while (firstDay.getDay() != 1){ // пока не понедельник
        firstDay.setDate(firstDay.getDate() - 1);
    }
    while (lastDay.getDay() != 0){ // пока не воскресенье
        lastDay.setDate(lastDay.getDate() + 1);
    }
    return firstDay.getDate() + " " + getMonthByNumber(firstDay.getMonth() + 1) + " - " +
        lastDay.getDate() + " " + getMonthByNumber(lastDay.getMonth() + 1);
}

function fillTableHeader(viewType, minDate, maxDate, holidays, tableRowCount){

    var currentDate = new Date();
    currentDate.setFullYear(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate());
    var dTemp = new Date();
    var firstRowStr = "";
    var thirdRow = "";
    var gStr = "";
    var colSpan = 0;
    var counter = 0;
    var tableHeight = getTableHeight(tableRowCount);

    // разлинуем в разный цвет четные и нечетные строки
    var dateDiff = dateDiffInDay(minDate, maxDate) + 1;
    var width = tableColumnWidthKoef * dateDiff + tableColumnWidthForEmployeeName;
    var topLine = topAfterHeader;
    for (var i = 0; i < tableRowCount; i++ ){
        if (i % 2 == 0){
            gStr += "<div style='position:absolute; top:" + topLine + "px; left:1px; width:" + width + "px; height: 16px; background-color:rgb(240,240,240);'></div>";
        }
        topLine += tableRowHeightKoef;
    }

    /*
        чтобы дивки разлиновки отображались под таблицей (а не перекрывали таблицу) - таблице был задан параметр абсолютного позиционирования
        соответсвтенно дивка содержащая график нерастягивается, относительно размеров таблице
        поэтому была создана невидимая таблица с relative позиционированием, для того, чтобы расятнуть дивку вкладки
        шапка + высота нижней ячейки
    */
    firstRowStr = "<table style='visibility: false;height:" + (tableHeight + 50) + "px;'></table>";
    firstRowStr += "<table id='tableGraphic' border=1 style='border-collapse:collapse;position: absolute; left:0px; top: 0px'><tr><td style='height: " + tableHeaderHeight + "px' rowspan='2' style='width:" + tableColumnWidthForEmployeeName + "px;'><div class='GVacationTitle' style='width:200px;'>Сотрудник</div></td>";
    firstRowStr = gStr + firstRowStr; // сперва поместим дивки, потом таблицу

    gStr += "</tr><tr>";
    thirdRow = "<tr><td>&nbsp;</td>";
    dTemp.setFullYear(minDate.getFullYear(), minDate.getMonth(), minDate.getDate());

    if (viewType == VIEW_GRAPHIC_BY_DAY){
        while(Date.parse(dTemp) <= Date.parse(maxDate)){
            var cssClass = "GDay";
            if (Date.parse(dTemp) == Date.parse(currentDate)){
                cssClass = "GToday";
            }else
                /* Правильно было бы использовать тут проверку holidays.indexOf(dTemp.toLocaleDateString()) >= 0
                   но ИЕ8 не поддерживает эту функцию у массивов, поэтому используем функцию dojo
                */
                if(dojo.indexOf(holidays, dTemp.toLocaleDateString()) >= 0){ //Weekend
                    cssClass = "GWeekend";
            }
            gStr += "<td class='" + cssClass + "'><div style='width:" + (tableColumnWidthKoef - 1) + "px;'>" + dTemp.getDate() + "</div></td>";
            thirdRow += "<td id='GC_" + (counter++) + "' class='" + cssClass + "' style='height:" + (tableHeight) + "px'>&nbsp;</td>";
            if(dTemp.getDate() < getDaysInMonth(dTemp.getMonth() + 1, dTemp.getFullYear())){
                colSpan++;
            }else{
                firstRowStr += "<td class='GMonth' align='center' colspan='" + (colSpan + 1) + "'>" + getMonthByNumber(dTemp.getMonth() + 1) + " " + dTemp.getFullYear() + "</td>";
                colSpan = 0;
            }
            dTemp.setDate(dTemp.getDate() + 1);
        }
        if (colSpan != 0) {
            firstRowStr += "<td class='GMonth' align='center' colspan='" + (colSpan + 1) + "'>" + getMonthByNumber(dTemp.getMonth() + 1) + " " + dTemp.getFullYear() + "</td>";
        }
    }else{ // BY_WEEK
        while(Date.parse(dTemp) <= Date.parse(maxDate)){
            gStr += "<td class='GDay' title='" + makeMonthDivTitle(dTemp) + "'><div style='width:" + ((tableColumnWidthKoef - 1)*7 + 6) + "px;'>" + getWeek(dTemp) + "</div></td>";
            thirdRow += "<td id='GC_" + (counter++) + "' class='GDay' style='height:" + (tableHeight) + "px'>&nbsp;</td>";
            dTemp = addWeek(dTemp);
            colSpan++;
        }
        firstRowStr += "<td class='GMonth' align='center' colspan='" + (colSpan + 1) + "'>&nbsp;</td>";

        // месяцы поместим отдельной дивкой, т.к. параллели строк месяцев и недель не совпадают
        colSpan = 0;
        dTemp.setFullYear(minDate.getFullYear(), minDate.getMonth(), minDate.getDate());
        var strMonthDiv = "<tr>";
        var strMonthDivBegin = "<div style='position: absolute;left: 201px; top: 0px; height: 5px'><table border=1><tr>";
        while(Date.parse(dTemp) <= Date.parse(maxDate)){
            strMonthDiv += "<td class='GDay'><div style='width:" + (tableColumnWidthKoef - 1) + "px;'></div></td>";
            if(dTemp.getDate() < getDaysInMonth(dTemp.getMonth() + 1, dTemp.getFullYear())){
                colSpan++;
            }else{
                strMonthDivBegin += "<td class='GMonth' align='center' colspan='" + (colSpan + 1) + "'>" + getMonthByNumber(dTemp.getMonth() + 1) + " " + dTemp.getFullYear() + "</td>";
                colSpan = 0;
            }
            dTemp.setDate(dTemp.getDate() + 1);
        }
        strMonthDivBegin += "<td title='" + makeMonthDivTitle(dTemp) + "' class='GMonth' align='center' colspan='" + (colSpan + 1) + "'>" + getMonthByNumber(dTemp.getMonth() + 1) + " " + dTemp.getFullYear() + "</td>";
        strMonthDivBegin += "</tr>";
        strMonthDiv += "</tr></table></div>";
        firstRowStr = strMonthDivBegin + strMonthDiv + firstRowStr;
    }


    thirdRow += "</tr>";
    gStr += "</tr>" + thirdRow;
    gStr += "</table>";
    gStr = firstRowStr + gStr;

    return gStr;
}