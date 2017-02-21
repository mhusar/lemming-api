jQuery.ajaxSetup({
    cache : false
});

function changeFormTabOrder() {
    jQuery(".basePage form").on("keypress", function (event) {
        var tabulatorPressed = (event.keyCode === 9) ? true : false;
        var shiftPressed = event.shiftKey;

        if (tabulatorPressed && jQuery(event.target).is(":input")) {
            var formDescendants = jQuery(event.target).closest("form").find("*");
            var formInputs = formDescendants.filter(":input:visible").not(":disabled").not(":button").not(":submit");
            var firstInput = formInputs.first();
            var lastInput = formInputs.last();

            if (firstInput.is(event.target) && shiftPressed) {
                lastInput.focus();
                event.preventDefault();
            } else if (lastInput.is(event.target) && !(shiftPressed)) {
                firstInput.focus();
                event.preventDefault();
            }
        }
    });
}

jQuery(document).ready(function () {
    changeFormTabOrder();
});

jQuery.fn.isInViewport = function (offset) {
    var elementTop, elementBottom, viewportTop, viewportBottom;

    if (typeof offset === "undefined") {
        offset = 0;
    }

    elementTop = jQuery(this).offset().top;
    elementBottom = elementTop + jQuery(this).outerHeight();
    viewportTop = jQuery(window).scrollTop();
    viewportBottom = viewportTop + jQuery(window).height();

    return elementBottom - offset > viewportTop && elementTop + offset < viewportBottom;
};

jQuery.debounce = function (delay, callback) {
    var timeout = null;

    return function () {
        var _arguments = arguments;

        if (timeout) {
            clearTimeout(timeout);
        }

        timeout = setTimeout(function () {
            callback.apply(null, _arguments);
            timeout = null;
        }, delay);
    };
}
