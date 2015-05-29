var DATE_TEXT_BOX_EXT_PATH = "com.aplana.dijit.ext";

dojo.require("dijit.form.DateTextBox");
dojo.provide(DATE_TEXT_BOX_EXT_PATH);

dojo.declare(DATE_TEXT_BOX_EXT_PATH + ".DateTextBox", dijit.form.DateTextBox, {
    isDisabledDate: function() {
        return false;
    },

    openDropDown: function() {
        this.inherited(arguments);

        this.dropDown.isDisabledDate = this.isDisabledDate;

        this.dropDown._populateGrid();
    }
});