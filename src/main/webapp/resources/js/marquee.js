/**
 * Created by dsysterov on 21.05.2014.
 */

var aplanaMarquee = {

    /**
     * Создание из двух вложенных блоков DIV бегущей строки.
     * @param id Идентификатор внешнего блока.
     * @param delayTime Задержка перед первым циклом движения строки.
     * @param restTime Задержка перед каждым последующим циклом движением строки.
     */
    init: function(id, delayTime, restTime) {
        delayTime = (typeof(delayTime) == "number") ? delayTime : 0;
        restTime = (typeof(restTime) == "number") ? restTime : 0;

        var SPEED = 1;
        var INTERVAL = 15;

        var container = document.getElementById(id);
        container.style.overflow = "hidden";

        var content = container.getElementsByTagName("DIV")[0];
        content.style.display    = "inline";
        content.style.position   = "absolute";
        content.style.whiteSpace = "nowrap";
        content.style.left       = "0px";

        var marquee = this["aplanaMarquee_" + id] = {
            content:        content,
            container:      container,
            scrollDelay:    delayTime,
            speed:          SPEED,
            halt:           false,
            rest:           false,
            restTime:       restTime
        };

        // Старт интервала анимации для бегущей строки
        window.setTimeout(function() {
            marquee.animationInterval = setInterval(function() {
                aplanaMarquee.scroll(id);
            }, INTERVAL);
        }, marquee.scrollDelay);

        // Остановка при поднесении курсора мыши и возобновление движения при убирании курсора
        if (container.addEventListener) {
            container.addEventListener("mouseover", function() {aplanaMarquee.halt(id);}, false);
            container.addEventListener("mouseout", function() {aplanaMarquee.resume(id);}, false);
        } else if (container.attachEvent) {
            container.attachEvent("onmouseover", function() {aplanaMarquee.halt(id);});
            container.attachEvent("onmouseout", function() {aplanaMarquee.resume(id);});
        } else {
            container["onmouseover"] = function() {aplanaMarquee.halt(id);};
            container["onmouseout"] = function() {aplanaMarquee.resume(id);};
        }

    },

    /**
     * Один шаг анимации бегущей строки.
     */
    scroll: function(id) {
        var marquee = this["aplanaMarquee_" + id];

        if (marquee && !marquee.halt && !marquee.rest) {
            var position = parseInt(marquee.content.style.left) - marquee.speed;
            if (position < -marquee.content.scrollWidth) {
                // Возврат строки за правую границу контейнера
                position = marquee.container.scrollWidth + 10;
            } else if (position == 0) {
                // Остановка строки между циклами
                marquee.rest = true;
                window.setTimeout(function() {marquee.rest = false}, marquee.restTime);
            }
            marquee.content.style.left = position + "px";
        }
    },

    /**
     * Остановка бегущей строки.
     */
    halt: function(id) {
        var marquee = this["aplanaMarquee_" + id];

        if (marquee) {
            marquee.halt = true;
        }
    },

    /**
     * Отмена остановки бегущей строки.
     */
    resume: function(id) {
        var marquee = this["aplanaMarquee_" + id];

        if (marquee) {
            marquee.halt = false;
        }
    }
};