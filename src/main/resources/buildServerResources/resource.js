
BS.EditResourceForm = OO.extend(BS.AbstractWebForm, {
    formElement : function() {
        return $('editResourceForm');
    },

    saveResource : function() {
        this.formElement().submitAction.value = 'saveResource';

        BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener, {

          onCompleteSave : function(form, responseXML, err) {
            form.enable();
            if (!err) {
              BS.EditResourceDialog.close();
            }
          }
        }), false);

        return false;
    },

    removeResource : function(name) {
        if (!confirm("Are you sure you want to remove this resource?")) return;

        var url = this.formElement().action + "&submitAction=removeResource&resourceName=" + name;
        BS.ajaxRequest(url, {
          onComplete: function() {
            BS.EditResourceDialog.close();
          }
        });
    }
});

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
