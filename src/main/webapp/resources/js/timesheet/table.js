/* Добавляет новую строку в табличную часть отчёта. */
function addNewRow() {
    var tsTable = dojo.byId("time_sheet_table");
    var tsRows = dojo.query(".time_sheet_row");
    var tsRowCount = tsRows.length; // Количество активных строк таблицы.
    var tsTableTrCount = tsRows.length + 2; // +2 это tr с заголовком и tr с ИТОГО
    // Добавляем новую строку перед строкой ИТОГО
    var newTsRow = tsTable.insertRow(tsTableTrCount - 1);
    dojo.addClass(newTsRow, "time_sheet_row");
    // Индекс новой строки
    var newRowIndex;
    if (tsRowCount == 0) {
        newRowIndex = 0;
    }
    else {
        // Получаем идентификатор последней строки для получения ее индекса (не
        // путать с номером строки, они могут не совпадать).
        var lastRow = tsRows[tsRowCount - 1];
        var lastRowId = dojo.attr(lastRow, "id");
        var lastRowIndex = parseInt(lastRowId.substring(lastRowId.lastIndexOf("_") + 1, lastRowId.length));
        newRowIndex = lastRowIndex + 1;
    }
    dojo.attr(newTsRow,
        { id: "ts_row_" + newRowIndex });

    //ячейка с кнопкой(картинкой) удаления строки
    var deleteCell = newTsRow.insertCell(0);
    dojo.addClass(deleteCell, "text_center_align");
    var img = dojo.doc.createElement("img");
    dojo.addClass(img, "pointer");
    dojo.attr(img, {
        id: "delete_button_" + newRowIndex,
        src: "resources/img/delete.png",
        alt: "Удалить",
        title: "Удалить",
        //без px так как IE не понимает
        height: "15",
        width: "15"
    });
    dojo.attr(img, "class", "controlToHide");
    // неведома ошибка исправляется для IE добавлением onclick именно через функцию
    img.onclick = function () {
        deleteRow(newRowIndex);
    };
    deleteCell.appendChild(img);

    // Ячейка с номером строки
    var rowNumCell = newTsRow.insertCell(1);
    dojo.addClass(rowNumCell, "text_center_align row_number");
    rowNumCell.innerHTML = tsRowCount + 1;

    // Ячейка с типами активности
    var actTypeCell = newTsRow.insertCell(2);
    dojo.addClass(actTypeCell, "top_align");
    var actTypeSelect = dojo.doc.createElement("select");
    dojo.attr(actTypeSelect, {
        id: "activity_type_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].activityTypeId"
    });
    dojo.addClass(actTypeSelect, "activityType");
    insertEmptyOption(actTypeSelect);
    for (var i = 0; i < actTypeList.length; i++) {
        var actTypeOption = dojo.doc.createElement("option");
        dojo.attr(actTypeOption, {
            value: actTypeList[i].id
        });
        actTypeOption.title = actTypeList[i].value;

        actTypeOption.innerHTML = actTypeList[i].value;
        actTypeSelect.appendChild(actTypeOption);
    }
    actTypeCell.appendChild(actTypeSelect);

    // Ячейка с местом работы
    var workplaceCell = newTsRow.insertCell(3);
    dojo.addClass(workplaceCell, "top_align");
    var workplaceSelect = dojo.doc.createElement("select");
    dojo.attr(workplaceSelect, {
        id: "workplace_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].workplaceId"
    });
    dojo.addClass(workplaceSelect, "workplace");
    fillWorkplaceSelect(workplaceSelect);
    workplaceCell.appendChild(workplaceSelect);

    // Ячейка с названиями проектов/пресейлов
    var projectNameCell = newTsRow.insertCell(4);
    dojo.addClass(projectNameCell, "top_align");
    var projectSelect = dojo.doc.createElement("select");
    dojo.attr(projectSelect, {
        id: "project_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].projectId"
    });
    insertEmptyOption(projectSelect);
    projectNameCell.appendChild(projectSelect);

    // Ячейка с проектной ролью
    var projectRoleCell = newTsRow.insertCell(5);
    dojo.addClass(projectRoleCell, "top_align");
    var projectRoleSelect = dojo.doc.createElement("select");
    dojo.attr(projectRoleSelect, {
        id: "project_role_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].projectRoleId"
    });
    insertEmptyOption(projectRoleSelect);
    for (var i = 0; i < projectRoleList.length; i++) {
        var projectRoleOption = dojo.doc.createElement("option");
        dojo.attr(projectRoleOption, {
            value: projectRoleList[i].id
        });
        projectRoleOption.title = projectRoleList[i].value;

        projectRoleOption.innerHTML = projectRoleList[i].value;
        projectRoleSelect.appendChild(projectRoleOption);
    }
    sortSelectOptions(projectRoleSelect);
    projectRoleCell.appendChild(projectRoleSelect);

    // Ячейка с категорией активности
    var actCatCell = newTsRow.insertCell(6);
    dojo.addClass(actCatCell, "top_align");
    var actCatSelect = dojo.doc.createElement("select");
    dojo.attr(actCatSelect, {
        id: "activity_category_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].activityCategoryId",
        onchange: "setActDescription(" + newRowIndex + ")"
    });
    insertEmptyOption(actCatSelect);
    actCatCell.appendChild(actCatSelect);
    var labelDescription = dojo.doc.createElement("label");
    dojo.attr(labelDescription, {
        id: "act_description_" + newRowIndex,
        style: "font-style: italic"
    });
    actCatCell.appendChild(labelDescription);

    // Ячейка с проектными задачами
    var projectTasksCell = newTsRow.insertCell(7);
    dojo.addClass(projectTasksCell, "top_align");
    var projectTasksSelect = dojo.doc.createElement("select");
    dojo.attr(projectTasksSelect, {
        id: "projectTask_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].projectTaskId",
        onchange: "setTaskDescription(" + newRowIndex + ")"
    });
    insertEmptyOption(projectTasksSelect);
    var taskLabelDescription = dojo.doc.createElement("label");
    dojo.attr(taskLabelDescription, {
        id: "task_description_" + newRowIndex,
        style: "font-style: italic"
    });

    projectTasksCell.appendChild(projectTasksSelect);
    projectTasksCell.appendChild(taskLabelDescription);

    // Ячейка с часами
    var durationCell = newTsRow.insertCell(8);
    dojo.addClass(durationCell, "top_align");
    var durationInput = dojo.doc.createElement("input");
    dojo.attr(durationInput, {
        id: "duration_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].duration",
        //type:"number",
        type: "text"
    });
    dojo.addClass(durationInput, "text_right_align duration");
    durationCell.appendChild(durationInput);

    // Ячейка с комментариями
    var descriptionCell = newTsRow.insertCell(9);
    dojo.addClass(descriptionCell, "top_align");
    var descriptionTextarea = dojo.doc.createElement("textarea");
    dojo.attr(descriptionTextarea, {
        id: "description_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].description",
        wrap: "soft",
        rows: "3",
        style: "width: 100%"
    });
    descriptionCell.appendChild(descriptionTextarea);

    //ячейка с кнопкой(картинкой) запроса из JIRA
    var jiraCell = newTsRow.insertCell(10);
    dojo.addClass(jiraCell, "text_center_align");
    var jiraImg = dojo.doc.createElement("img");
    dojo.addClass(jiraImg, "pointer");
    dojo.attr(jiraImg, {
        id: "jira_button_" + newRowIndex,
        // class:"controlToHide",
        src: "resources/img/logo-jira.png",
        alt: "Запрос из JIRA",
        title: "Запрос из JIRA",
        //без px так как IE не понимает
        height: "15",
        width: "15",
        style: "cursor:pointer;"
    });
    dojo.attr(jiraImg, "class", "controlToHide");

    //неведома ошибка исправляется для IE добавлением onclick именно через функцию
    jiraImg.onclick = function () {
        getJiraInfo(newRowIndex);
    };
    jiraCell.appendChild(jiraImg);

    // Ячейка с проблемами
    var problemCell = newTsRow.insertCell(11);
    dojo.addClass(problemCell, "top_align");
    var problemTextarea = dojo.doc.createElement("textarea");
    dojo.attr(problemTextarea, {
        id: "problem_id_" + newRowIndex,
        name: "timeSheetTablePart[" + newRowIndex + "].problem",
        wrap: "soft",
        rows: "3",
        style: "width: 100%"
    });
    problemCell.appendChild(problemTextarea);

    // Помещаем новую строку в конец таблицы
    recalculateRowNumbers();
    resetRowState(newRowIndex, true);

    /*подключаем функции показа тултипов и регистрации изменений для селктов */
    setDefaultSelectEvents(workplaceSelect);
    //для типа активности
    setDefaultSelectEvents(actTypeSelect);
    //для проекта
    setDefaultSelectEvents(projectSelect);
    //для проектной роли
    setDefaultSelectEvents(projectRoleSelect);
    //для категории активности
    setDefaultSelectEvents(actCatSelect);
    //для задачи
    setDefaultSelectEvents(projectTasksSelect);

    /*для инпутов подключаем только регистрацию изменений по нажатию*/
    dojo.connect(durationInput, "onkeyup", durationInput, somethingChanged);
    dojo.connect(descriptionTextarea, "onkeyup", descriptionTextarea, somethingChanged);
    dojo.connect(problemTextarea, "onkeyup", problemTextarea, somethingChanged);

    dojo.connect(actTypeSelect, "onchange", actTypeSelect, typeActivityChange);
    dojo.connect(projectSelect, "onchange", projectSelect, projectChange);
    dojo.connect(projectRoleSelect, "onchange", projectRoleSelect, projectRoleChange);
    dojo.connect(durationInput, "onchange", durationInput, checkDuration);
    dojo.connect(descriptionTextarea, "onkeyup", descriptionTextarea, textareaAutoGrow);
    dojo.connect(problemTextarea, "onkeyup", problemTextarea, textareaAutoGrow);

    dojo.connect(actTypeSelect, "onchange", actTypeSelect, deleteEmptyOption);
    dojo.connect(workplaceSelect, "onchange", workplaceSelect, deleteEmptyOption);
    dojo.connect(projectSelect, "onchange", projectSelect, deleteEmptyOption);
    dojo.connect(projectRoleSelect, "onchange", projectRoleSelect, deleteEmptyOption);
    dojo.connect(projectSelect, "onchange", projectSelect, deleteEmptyOption);
    dojo.connect(actCatSelect, "onchange", actCatSelect, deleteEmptyOption);
    dojo.connect(projectTasksSelect, "onchange", projectTasksSelect, deleteEmptyOption);
}

/* удаляет пустой option */
function deleteEmptyOption() {
    var selected = this.options[0];
    if (selected.value == 0) {
        this.remove(0);
    }
}

function setDefaultSelectEvents(obj) {
    dojo.connect(obj, "onmouseover", obj, showTooltip);
    dojo.connect(obj, "onmouseout", obj, hideTooltip);
    dojo.connect(obj, "onmouseup", obj, somethingChanged);
    dojo.connect(obj, "onkeyup", obj, somethingChanged);
}

/* Возвращает массив текущих Id строк в таблице */
function getRowsId(obj) {
    var listId = [];
    for (var i = 0; i < obj.length; i++) {
        var id = dojo.attr(obj[i], "id");
        var id_num = parseInt(id.substring(id.lastIndexOf("_") + 1, id.length));
        listId[i] = id_num;
    }
    return listId;
}

/*Проверяем, есть ли в табличной части отчета непустые строки
 * когда кликаем на чекбокс отпуска/болезни
 * если находим, передаем тру, и потом эти строки очищаем.*/
function tablePartNotEmpty() {
    var notEmpty = false;
    var tsRows = getRowsId(dojo.query(".time_sheet_row"));
    for (var i = 0; i < tsRows.length; i++) {
        var actTypeSelect = dojo.byId('activity_type_id_' + tsRows[i]);
        if (actTypeSelect.value != 0) {
            notEmpty = true;
            break;
        }
    }
    return notEmpty;
}

/*
 * Восстанавливает содержимое компонентов каждой строки табличной части отчёта
 * после возврата страницы валидатором.
 */
function reloadRowsState() {
    var rowsCount = dojo.query(".time_sheet_row").length;
    var rows = dojo.query(".time_sheet_row");
    for (var i = 0; i < rowsCount; i++) {

        var projectSelect = dojo.byId("project_id_" + i);
        if (dojo.attr(projectSelect, "disabled") != "disabled") {
            for (var k = 0; k < selectedProjects.length; k++) {
                if (selectedProjects[k].row == i) {
                    dojo.attr(projectSelect, { value: selectedProjects[k].project });
                }
            }
            projectChange(projectSelect);
        }

        var projectRoleSelect = dojo.byId("project_role_id_" + i);
        if (dojo.attr(projectRoleSelect, "disabled") != "disabled") {
            for (var p = 0; p < selectedProjectRoles.length; p++) {
                if (selectedProjectRoles[p].row == i) {
                    dojo.attr(projectRoleSelect, { value: selectedProjectRoles[p].role });
                }
            }
        }

        var actCatSelect = dojo.byId("activity_category_id_" + i);
        if ((dojo.attr(actCatSelect, "disabled") != "disabled")) {
            for (var q = 0; q < selectedActCategories.length; q++) {
                if (selectedActCategories[q].row == i) {
                    fillAvailableActivityCategoryList(i);
                    dojo.attr(actCatSelect, { value: selectedActCategories[q].actCat });
                }
            }
        }
        setActDescription(i);

        var taskSelect = dojo.byId("projectTask_id_" + i);
        if (dojo.attr(taskSelect, "disabled") != "disabled") {
            for (var j = 0; j < selectedProjectTasks.length; j++) {
                if (selectedProjectTasks[j].row == i) {
                    dojo.attr(taskSelect, { value: selectedProjectTasks[j].task });
                }
            }
        }
        setTaskDescription(i);

        if (dojo.byId("delete_button_" + i) === null || dojo.byId("delete_button_" + i) === undefined) {
            var deleteCell = rows[i].cells[0];
            var img = dojo.doc.createElement("img");
            dojo.addClass(img, "pointer");
            dojo.attr(img, {
                id: "delete_button_" + i,
                src: "resources/img/delete.png",
                alt: "Удалить",
                title: "Удалить",
                //без px так как IE не понимает
                height: "15",
                width: "15"
            });

            //неведома ошибка исправляется для IE добавлением onclick именно через функцию
            //индускод
            img.onclick = function () {
                var id = dojo.attr(this, "id");
                var id_num = parseInt(id.substring(id.lastIndexOf("_") + 1, id.length));
                console.log(id_num);
                deleteRow(id_num);
            };
            deleteCell.appendChild(img);
        }

        //ячейка с кнопкой(картинкой) запроса из JIRA
        if (dojo.byId("jira_button_" + i) === null || dojo.byId("jira_button_" + i) === undefined) {
            var jiraCell = rows[i].cells[10];
            var jiraImg = dojo.doc.createElement("img");
            dojo.addClass(jiraImg, "pointer");
            dojo.attr(jiraImg, {
                id: "jira_button_" + i,
                src: "resources/img/logo-jira.png",
                alt: "Запрос из JIRA",
                title: "Запрос из JIRA",
                //без px так как IE не понимает
                height: "15",
                width: "15"
            });

            //неведома ошибка исправляется для IE добавлением onclick именно через функцию
            jiraImg.onclick = function () {
                getJiraInfo(newRowIndex);
            };
            jiraCell.appendChild(jiraImg);
        }
    }

    recalculateDuration();

    if (dataDraft != null && dataDraft == "true") {
        loadDraft();
    }
}

/*
 * Очищает содержимое компонентов каждой строки табличной части отчёта.
 * Если resetActType == true, "Тип активности" тоже очищается.
 */
function resetRowState(rowIndex, resetActType) {
    if (resetActType) {
        //dojo.byId("selected_row_id_" + rowIndex).checked = false;
        dojo.attr("activity_type_id_" + rowIndex, {
            value: "0"
        });
    }

    var disabledText = "disabled";
    dojo.attr("workplace_id_" + rowIndex, {
        disabled: disabledText,
        value: "0"
    });

    dojo.attr("project_id_" + rowIndex, {
        disabled: disabledText,
        value: "0"
    });
    dojo.attr("project_role_id_" + rowIndex, {
        disabled: disabledText,
        value: "0"
    });
    dojo.attr("activity_category_id_" + rowIndex, {
        disabled: disabledText,
        value: "0"
    });
    var labelDescription = dojo.byId("act_description_" + rowIndex);
    labelDescription.innerHtml = "";
    setActDescription(rowIndex);
    dojo.attr("projectTask_id_" + rowIndex, {
        disabled: disabledText,
        value: "0"
    });
    dojo.byId("description_id_" + rowIndex).value = "";
    dojo.attr("description_id_" + rowIndex, {
        disabled: disabledText
    });

    dojo.byId("problem_id_" + rowIndex).value = "";
    dojo.attr("problem_id_" + rowIndex, {
        disabled: disabledText
    });

    dojo.attr("duration_id_" + rowIndex, {
        disabled: disabledText,
        value: ""
    });
    recalculateDuration();
}

/* Производит пересчёт номеров строк табличной части отчёта. */
function recalculateRowNumbers() {
    var rows = dojo.query(".row_number");
    for (var i = 0; i < rows.length; i++) {
        rows[i].innerHTML = i + 1;
    }
}

function deleteRow(id_row) {
    var tsRow = document.getElementById("ts_row_" + id_row);
    if (tsRow !== null) {
        tsRow.parentNode.removeChild(tsRow);
    }
    recalculateRowNumbers();
    recalculateDuration();
}