
BS.EditResourceDialog = OO.extend(BS.AbstractModalDialog, {
    getContainer : function() {
        return $('editResourceDialog');
    },

    showDialog : function(name, host, port) {
        $('resourceName').value = name;
        $('resourceHost').value = host;
        $('resourcePort').value = port;

        var title = name.length == 0 ? 'Add New Resource' : 'Edit Resource';
        $('resourceDialogTitle').innerHTML = title;

        this.showCentered();
        $('resourceName').focus();
    },

    cancelDialog : function() {
        this.close();
    }
});
