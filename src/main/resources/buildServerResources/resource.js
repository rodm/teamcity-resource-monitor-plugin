
BS.EditResourceDialog = OO.extend(BS.AbstractModalDialog, {
    getContainer : function() {
        return $('editResourceDialog');
    },

    showDialog : function() {
        $('resourceDialogTitle').innerHTML = 'Add New Resource';
        this.showCentered();
    },

    cancelDialog : function() {
        this.close();
    }
});
