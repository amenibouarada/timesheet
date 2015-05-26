dojo.require("dojox.widget.Standby");
var standByElement;
dojo.ready(function () {
    //крутилка создается при после загрузки страницы,
    //т.к. если она создается в месте использования - ghb show не отображается картинка
    standByElement = new dojox.widget.Standby({target: dojo.query("body")[0], zIndex: 1000});
});

String.prototype.trim = function () {
    return this.replace(/^\s+|\s+$/g, '');
};

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

function clearErrorBox(divId) {
    var target = dojo.byId(divId);
    if (target != null) {
        dojo.destroy(divId);
    }
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

// возвращает первый рабочий день сотрудника
function getFirstWorkDate(employee) {
    return getDateByString(employee.firstWorkDate);
}

// возващает последний рабочий день сотрудника
function getLastWorkDate(employee) {
    return getDateByString(employee.lastWorkDate);
}

/* Создает cookie с указанными параметрами */
function setCookie(name, value, exp, pth, dmn, sec) {
    document.cookie = name + '=' + escape(value)
        + ((exp) ? '; expires=' + exp : '')
        + ((pth) ? '; path=' + pth : '')
        + ((dmn) ? '; domain=' + dmn : '')
        + ((sec) ? '; secure' : '');
}

function getRootEventListener() {
    return window.addEventListener || window.attachEvent ? window : document.addEventListener ? document : null;
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
