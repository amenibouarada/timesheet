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

function isNumber(n) {
    return (typeof n != typeof undefined) && !isNaN(parseFloat(n)) && isFinite(n);
}

function isNilOrNull(obj) {
    return !(obj != null && obj != 0);
}

function isUndefinedNullNaN(value) {
    return value == NaN || value == null || value == undefined;
}

