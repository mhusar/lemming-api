jQuery.ajaxSetup({
    cache : false
});

jQuery(document).ready(function() {
    fixRequiredAttributeForSafari();
    setupFeedbackPanel();
    restrictTableColumnText();

    setupApostropheKey();
    changeFormTabOrder();

    enableCategoryAutoComplete();
    enableSiglumAutoComplete();
    enableSiglumCheck();
});

jQuery(document).ajaxComplete(function() {
    restrictTableColumnText();
});

// see http://caniuse.com/#feat=form-validation
function fixRequiredAttributeForSafari() {
    if (navigator.userAgent.indexOf("Safari") != -1
            && navigator.userAgent.indexOf("Chrome") == -1) {
        jQuery("form").submit(function(event) {
            var requiredFields = jQuery(this).find("[required]");

            requiredFields.each(function() {
                if (jQuery(this).val() == "") {
                    alert("Bitte alle erforderlichen Felder ausfüllen.");
                    jQuery(this).focus();
                    event.preventDefault();

                    return false;
                }
            });

            return true;
        });
    }
}

function setupFeedbackPanel(id) {
    var listItems;

    if (typeof id !== "undefined") {
        listItems = jQuery(id + " li");
    } else {
        listItems = jQuery(".feedbackPanel li");
    }

    listItems.each(function(index) {
        var listElementClass = jQuery(this).attr("class");

        jQuery(this).removeClass(listElementClass).addClass(
                "alert alert-" + listElementClass);
    });
}

function restrictTableColumnText() {
    jQuery("table td div").each(
            function(index) {
                if (jQuery(this).children().length === 0) {
                    if (jQuery(this).text().length > 40) {
                        jQuery(this).attr("title", jQuery(this).text());
                        jQuery(this).text(
                                jQuery.trim(jQuery(this).text()
                                        .substring(0, 39)));
                        jQuery(this).append("…");
                    }
                }
            });
}

function setupApostropheKey() {
    jQuery(":input:text, textarea")
            .keydown(
                    function(event) {
                        var character = "’", formElement = jQuery(this), selectionStart, selectionEnd;

                        if (event.shiftKey && event.which === 163) {
                            event.preventDefault();

                            selectionStart = formElement[0].selectionStart;
                            selectionEnd = formElement[0].selectionEnd;

                            insertCharacter(formElement, character,
                                    selectionStart, selectionEnd);
                        }
                    });
}

function insertCharacter(formElement, character, selectionStart, selectionEnd) {
    var value = formElement.val();

    if (selectionStart <= selectionEnd) {
        formElement.val(value.substring(0, selectionStart) + character
                + value.substring(selectionEnd));
        formElement[0]
                .setSelectionRange(selectionStart + 1, selectionStart + 1);
    } else {
        formElement.val(value.substring(0, selectionEnd) + character
                + value.substring(selectionStart));
        formElement[0].setSelectionRange(selectionEnd + 1, selectionEnd + 1);
    }
}

function changeFormTabOrder() {
    jQuery(".basePage form").on(
            "keypress",
            function(event) {
                var tabulatorPressed = (event.keyCode === 9) ? true : false;
                var shiftPressed = event.shiftKey;

                if (tabulatorPressed && jQuery(event.target).is(":input")) {
                    var formDescendants = jQuery(event.target).closest("form")
                            .find("*");
                    var formInputs = formDescendants.filter(":input:visible")
                            .not(":disabled").not(":button").not(":submit");
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

function enableCategoryAutoComplete() {
    if (typeof categorySelector !== "undefined") {
        jQuery(categorySelector).autocomplete({
            autoFocus : true,
            delay : 0,
            source : categoryCallbackUrl
        });
    }
}

function enableSiglumAutoComplete() {
    if (typeof siglumSelector !== "undefined") {
        jQuery(siglumSelector).autocomplete({
            autoFocus : true,
            delay : 0,
            source : siglumCallbackUrl
        });
    }
}

function enableSiglumCheck() {
    if (typeof siglumCheckSelector !== "undefined") {
        checkSiglum();
        jQuery(siglumCheckSelector).blur(checkSiglum);
        jQuery(siglumCheckSelector).change(checkSiglumEmpty);
        jQuery(siglumCheckSelector).on("autocompleteclose", checkSiglum);
        jQuery(siglumCheckSelector).on("autocompletefocus", checkFocusedSiglum);
        jQuery(siglumCheckSelector).on("autocompleteresponse",
                checkFocusedSiglum);
    }
}

function checkSiglum() {
    jQuery.ajax({
        url : siglumCheckCallbackUrl,
        dataType : "text",
        data : {
            "siglum" : jQuery(siglumCheckSelector).val()
        }
    }).done(function(data) {
        if (data === "true") {
            jQuery("body").removeClass("bg-danger");
        } else if (data === "false") {
            jQuery("body").addClass("bg-danger");
        }
    });
}

function checkSiglumEmpty() {
    if (jQuery("body").hasClass("bg-danger")) {
        if (jQuery(this).val().length === 0) {
            jQuery("body").removeClass("bg-danger");
        }
    }
}

function checkFocusedSiglum(event, ui) {
    var value = "";

    if (ui.hasOwnProperty("item")) {
        value = ui.item.value;
    } else if (ui.hasOwnProperty("content")) {
        if (ui.content.length) {
            value = ui.content[0].value;
        }
    }

    if (value.length) {
        jQuery.ajax({
            url : siglumCheckCallbackUrl,
            dataType : "text",
            data : {
                "siglum" : value
            }
        }).done(function(data) {
            if (data === "true") {
                jQuery("body").removeClass("bg-danger");
            } else if (data === "false") {
                jQuery("body").addClass("bg-danger");
            }
        });
    } else {
        jQuery("body").removeClass("bg-danger");
    }
}