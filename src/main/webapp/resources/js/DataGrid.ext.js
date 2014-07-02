dojo.require("dojox.grid.DataGrid");
dojo.require("dojo.data.ItemFileWriteStore");
dojo.require("dojo.parser");
dojo.require("dojo.NodeList-traverse");
dojo.require("dojo.cookie");

var hideButtonUrl = "/resources/img/hide.png";
var showButtonUrl = "/resources/img/show.png";

var COOKIE_PREFIX = "datagrid_hide_";
var HIDE_BUTTON_ID_PREFIX = "hide_button_";

function appendChildsRecursively(/* HTMLElement */ parent) {
    for (var i = 1; i < arguments.length; i++) {
        var child = arguments[i];

        if (typeof child == "string") {
            child = document.createElement(child);
        } else if (dojo.isArray(child)) {
            dojo.forEach(child, function(c) {
                parent.appendChild(c);
            });

            continue;
        }

        parent.appendChild(child);
        parent = child;
    }
}

function createNameWithHideButton(/* String */ name, /* String */ field) {
    var container = document.createElement("div");

    container.className = "colContainer";
    container.setAttribute("onmouseover", "showHideButton(this)");
    container.setAttribute("onmouseout", "hideHideButton(this)");

    var hideButton = document.createElement("img");
    var showButton = document.createElement("img");

    hideButton.id = HIDE_BUTTON_ID_PREFIX + field;
    hideButton.src = hideButtonUrl;
    hideButton.className = "hide_button";
    hideButton.setAttribute("onclick", "hideCol(this, '" + field + "')");
    hideButton.setAttribute("onmouseover", "tooltip.show('свернуть')");

    showButton.src = showButtonUrl;
    showButton.className = "show_button";
    showButton.setAttribute("onclick", "showCol(this, '" + field + "')");
    showButton.setAttribute(
            "onmouseover",
            "tooltip.show('развернуть &quot;' + dojo.query('.colTextHolder', this.parentNode)[0].innerHTML + '&quot;')"
    );

    hideButton.style.display = showButton.style.display = "none";
    hideButton.setAttribute("onmouseout", "tooltip.hide()");
    showButton.setAttribute("onmouseout", hideButton.getAttribute("onmouseout"));

    var nameContainer = document.createElement("div");

    nameContainer.className = "colTextHolder";
    nameContainer.innerHTML = name;

    container.appendChild(hideButton);
    appendChildsRecursively(container, "table", "tr", "td", [nameContainer, showButton]);

    return container.outerHTML;
}

/**
 * Header's view format:
 * {
 *      noscroll: <boolean>,
 *      expand: <boolean>,
 *      cells: [<{
 *          field: <String>,
 *          name: <String>,
 *          width: <String (<Number>px)>,
 *          noresize: <boolean>,
 *          editable: <boolean>
 *      }>],
 *      groups(optional): [<{
 *          name: <String>,
 *          colSpan: <int>,
 *          expand: <boolean> (expand from view will be ignored),
 *          cellsFormatter: <function>
 *      }>]
 * }
 *
 * groups - groups of cells (first row in multirow header)
 * cells - heaer's cells
 *
 * @return {Array}
 * @param headerViews
 */
function createLayout(/* Array */ headerViews) {
    var layout = [];

    dojo.forEach(headerViews, function(headerView) {
        var view = {
            noscroll: headerView.noscroll,
            cells: []
        };

        layout.push(view);

        var nogroups = (!dojo.isArray(headerView.groups) || headerView.groups.length === 0);
        var measure = "px";
        var padding = (dojo.isWebKit ? 1 : 1.25) + measure;
        var cellStyles = (view.noscroll === true || nogroups) ? undefined : "padding-left: " + padding + "; padding-right: " + padding + ";";

        dojo.forEach(headerView.cells, function(cell) {
            view.cells.push({
                field: cell.field,
                noresize: cell.noresize,
                width: cell.width,
                name: (nogroups && headerView.expand == true) ? createNameWithHideButton(cell.name, cell.field) : cell.name,
                editable: cell.editable,
                headerStyles:  (!cell.headerStyles ? "" : cell.headerStyles) + "width: " + cell.width,
                cellStyles: cellStyles,
                hasBeenChanged: {},
                formatter: function(text, rowIndex, cell) {
                    var div = document.createElement("div");

                    if (!this.editable) {
                        cell.customClasses.push("uneditableCell");
                    } else if (this.hasBeenChanged[rowIndex]) {
                        div.className = "editedCell";
                    }

                    var parentCol = this.parentCol;

                    if (!(parentCol && parentCol.isHidden)) {
                        div.innerHTML = (text.length != 0) ? text : '-';

                        if (parentCol && typeof parentCol.cellsFormatter == "function") {
                            div.innerHTML = parentCol.cellsFormatter(div.innerHTML);
                        }

                        var colElement = dojo.byId(this.id);

                        if (colElement) {
                            dojo.query(".colContainer .colTextHolder", colElement).forEach(function(item) {
                                div.style.display = item.style.display;
                            });
                        }
                    }

                    return div.outerHTML;
                }
            });
        });

        if (headerView.groups) {
            var groups = [];

            var offset = 0;
            var endIndex;

            dojo.forEach(headerView.groups, function(group) {
                var firstCellInGroup = headerView.cells[offset];
                var colSpan = group.colSpan;
                var groupCell = {
                    field: firstCellInGroup.field,
                    name: (group.expand == true)
                            ? createNameWithHideButton(group.name, firstCellInGroup.field)
                            : dojo.create("div", { innerHTML: group.name, className: "colContainer" }).outerHTML,
                    colSpan: colSpan,
                    childs: [],
                    isHidden: false,
                    cellsFormatter: group.cellsFormatter,
                    headerStylesTemp: !group.headerStyles ? "" : group.headerStyles,
                    updateWidth: function () {
                        var width = 0;

                        dojo.forEach(this.childs, function (child) {
                            width += parseFloat(child.width);
                        });

                        this.headerStyles = this.headerStylesTemp + "width: " + width + "px;";
                    }
                };

                function addChild(child) {
                    child.parentCol = groupCell;
                    child.srcName = child.name;
                    child.srcWidth = child.width;

                    groupCell.childs.push(child);
                }

                endIndex = offset + colSpan;

                for (var i = offset; i < endIndex; i++) {
                    addChild(view.cells[i]);
                }

                groupCell.updateWidth();

                groups.push(groupCell);

                offset += colSpan;
            });

            view.cells = [groups, view.cells];
            view.onBeforeRow = function(inDataIndex, inSubRows) {
                var hidden = (inDataIndex >= 0);

                for (var i = inSubRows.length - 2; i >= 0; i--) {
                    inSubRows[i].hidden = hidden;
                }
            }
        } else {
            view.cells = [view.cells];
        }
    });

    return layout;
}

function hideCol(button, colField) {
    switchColDisplay(button, colField, true, true);
}

function showCol(button, colField) {
    switchColDisplay(button, colField, false, true);
}

function getNatural (DOMelement) {
    var img = new Image();
    img.src = DOMelement.src;
    return {width: img.width, height: img.height};
}

function switchColDisplay(button, colField, /* Boolean */ hide, /* Boolean */ updateStructure) {
    var container = dojo.NodeList(button).parents(".colContainer")[0];
    var display = hide ? "none" : "";
    var buttonWidth = (button.style.width || (button.width + "px"));

    // Если IE8 или ниже
    if (buttonWidth == "0px"){
        var natural = getNatural(button);
        buttonWidth = natural.width;
    }

    var grid = dijit.byId(dojo.NodeList(container).parents(".dojoxGrid")[0].id);

    dojo.forEach(grid.structure, function(structure) {
        dojo.forEach(structure.cells[0], function(item) {
            var containsInChilds = (item.childs && dojo.some(item.childs, function(child) {
                return (child.field == colField);
            }));

            if (item.field == colField || containsInChilds) {
                if (containsInChilds) {
                    item.isHidden = hide;

                    var newWidth = (parseInt(buttonWidth) / item.childs.length) + "px";

                    dojo.forEach(item.childs, function(child) {
                        child.editable = hide ? !hide :
                            dojo.some(modelFieldsForSave, function (fieldForSave) {
                                return ((child.field == fieldForSave) && (child.field !== VACATION_PLAN_COLUMN));
                            }) && isEditable;

                        child.name = hide ? "<div>&nbsp;</div>" : child.srcName;
                        child.width = hide ? newWidth : child.srcWidth;
                    });

                    item.updateWidth();
                } else {
                    if (typeof item['srcWidth'] === typeof undefined) {
                        item['srcWidth'] = item.width;
                    }

                    item.width = hide ? buttonWidth : item.srcWidth;

                    child.editable = hide ? !hide :
                        dojo.some(modelFieldsForSave, function (fieldForSave) {
                            return ((child.field == fieldForSave) && (child.field !== VACATION_PLAN_COLUMN));
                        });
                }

                dojo.query(".colTextHolder", container).forEach(function(textHolder) {
                    textHolder.style.display = display;
                });

                hideHideButton(container);
                switchShowButtonDisplay(container, !hide);

                item.name = container.outerHTML;
            }
        });
    });

    dojo.cookie(COOKIE_PREFIX + colField, hide, { expire: -1 });
    dojo.cookie(COOKIE_PREFIX + colField, hide, { expire: 999999999 });

    if (updateStructure) {
        updateGridStructure(grid);
    }
}

function showHideButton(container) {
    switchHideButtonDisplay(container, false);
}

function hideHideButton(container) {
    switchHideButtonDisplay(container, true);
}

function switchHideButtonDisplay(container, hide) {
    hide |= (dojo.query(".colTextHolder", container)[0].style.display == "none");

    switchButtonDisplay(container, ".hide_button", hide);
}

function switchShowButtonDisplay(container, hide) {
    switchButtonDisplay(container, ".show_button", hide);
}

function switchButtonDisplay(container, cssClass, hide) {
    dojo.query(cssClass, container).forEach(function(button) {
        button.style.display = hide ? "none" : "";
    });
}

function normalize(/* Array */ modelFields, /* Array */ itemsToNormalize) {
    var items = [];

    var normalizedCopy;

    dojo.forEach(itemsToNormalize, function(item) {
        normalizedCopy = {};

        var value;

        dojo.forEach(modelFields, function(field) {
            value = item[field];

            var normValue = (dojo.isArray(value)) ? value[0] : value;
            normValue = ("" == normValue) ? 0 : normValue;
            normalizedCopy[field] = normValue;
        });

        items.push(normalizedCopy);
    });

    return items;
}

function restoreHiddenStateFromCookie(grid) {
    dojo.forEach(grid.structure, function(view) {
        dojo.forEach(view.cells[0], function(cell) {
            var field = cell.field;
            var cookieValue = dojo.cookie(COOKIE_PREFIX + field);

            if (cookieValue && cookieValue == "true") {
                switchColDisplay(dojo.byId(HIDE_BUTTON_ID_PREFIX + field), field, true, false);
            }
        });
    });

    updateGridStructure(grid);
}

function cellHasBeenEdited(grid, field, row) {
    dojo.forEach(grid.structure, function(structure) {
        dojo.forEach(structure.cells[0], function(item) {
            dojo.forEach(item.childs || [item], function(child) {
                child.hasBeenChanged[row] |= (child.field == field);
            });
        });
    });

    updateGridStructure(grid);
}

function getScrollableView(grid) {
    var scrollableViewIndex = -1;

    dojo.forEach(grid.structure, function(view, idx) {
        if (!view.noscroll) {
            scrollableViewIndex = idx;
        }
    });

    if (scrollableViewIndex >= 0) {
        var scrollBoxes = dojo.query(".dojoxGridMasterView .dojoxGridView .dojoxGridScrollbox", dojo.byId(grid.id));

        return scrollBoxes[scrollableViewIndex];
    }

    return null;
}

function updateGridStructure(grid) {
    var scrollableView = getScrollableView(grid);
    var srcScrollVertical = null;
    var srcScrollHorizontal = null;

    if (scrollableView) {
        srcScrollVertical = scrollableView.scrollTop;
        srcScrollHorizontal = scrollableView.scrollLeft;
    }

    grid.setStructure(grid.structure);

    // да-да, именно дважды! ибо после изменения структуры въюхи пересоздаются
    scrollableView = getScrollableView(grid);

    if (scrollableView) {
        if (srcScrollVertical) {
            setTimeout(function() {
                scrollableView.scrollTop = srcScrollVertical;
            }, 1);
        }

        if (srcScrollHorizontal) {
            setTimeout(function() {
                scrollableView.scrollLeft = srcScrollHorizontal;
            }, 1);
        }

        var scrollBoxes = dojo.query(".dojoxGridMasterView .dojoxGridView .dojoxGridScrollbox", dojo.byId(grid.id));

        dojo.forEach(scrollBoxes, function(scrollBox) {
            if (scrollBox == scrollableView) {
                return;
            }

            dojo.connect(scrollBox, (!dojo.isMozilla ? "onmousewheel" : "DOMMouseScroll"), function(e) {
                try {
                    var scroll = e[(!dojo.isMozilla ? "wheelDelta" : "detail")] * (!dojo.isMozilla ? 1 : -1);

                    scrollableView.scrollTop -= scroll;
                } catch(ex) {
                    console.log(ex);
                }
            });
        });
    }
}