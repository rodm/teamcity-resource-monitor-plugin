
BS.EditResourceForm = OO.extend(BS.AbstractWebForm, {
    formElement : function() {
        return $('editResourceForm');
    },

    saveResource : function() {
        var that = this;
        BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener, {

          onCompleteSave : function(form, responseXML, err) {
            form.enable();
            if (!err) {
              BS.EditResourceDialog.close();
              document.location.reload();
            }
          },

          onInvalidNameError : function(elem) {
              $('error_resourceName').innerHTML = elem.firstChild.nodeValue;
              that.highlightErrorField($('resourceName'));
          },

          onInvalidHostError : function(elem) {
              $('error_resourceHost').innerHTML = elem.firstChild.nodeValue;
              that.highlightErrorField($('resourceHost'));
          },

          onInvalidPortError : function(elem) {
              $('error_resourcePort').innerHTML = elem.firstChild.nodeValue;
              that.highlightErrorField($('resourcePort'));
          },

          onResourceError : function(elem) {
              alert(elem.firstChild.nodeValue);
          }
        }), false);

        return false;
    },

    removeResource : function(id) {
        if (!confirm("Are you sure you want to remove this resource?")) return;

        var url = this.formElement().action + "&submitAction=removeResource&resourceId=" + id;
        BS.ajaxRequest(url, {
          onComplete: function() {
            BS.EditResourceDialog.close();
            document.location.reload();
          }
        });
    }
});

BS.EditResourceDialog = OO.extend(BS.AbstractModalDialog, {
    getContainer : function() {
        return $('editResourceDialog');
    },

    showDialog : function (id, name, host, port, limit) {
        $('resourceId').value = id;
        $('resourceName').value = name;
        $('resourceHost').value = host;
        $('resourcePort').value = port;
        $('resourceLimit').value = limit;

        var title = name.length == 0 ? 'Add New Resource' : 'Edit Resource';
        $('resourceDialogTitle').innerHTML = title;
        var action = name.length == 0 ? 'addResource' : 'updateResource';
        $('submitAction').value = action;

        this.showCentered();
        $('resourceName').focus();
    },

    cancelDialog : function() {
        this.close();
    }
});

BS.Resource = {
    enableResource: function(id, enable) {
        var url = base_uri + "/resource.html?submitAction=";
        url = url + ((enable) ? "disableResource" : "enableResource");
        url = url + "&resourceId=" + id;
        BS.ajaxRequest(url, {
            onSuccess: function(transport) {
                document.location.reload();
            },
            onFailure: function() {
                alert('Unable to enable/disable resource');
            }
        });
    },

    linkBuildType: function(id, buildTypeId) {
        var url = base_uri + "/resource.html?submitAction=linkBuildType&resourceId=" + id + "&buildTypeId=" + buildTypeId;
        BS.ajaxRequest(url, {
            onSuccess: function(transport) {
                document.location.reload();
            },
            onFailure: function() {
                alert('Unable to link dependency');
            }
        });
    },

    unlinkBuildType : function(id, buildTypeId) {
        if (!confirm("Are you sure you want to remove this build configuration?")) return;

        var url = base_uri + "/resource.html?submitAction=unlinkBuildType&resourceId=" + id + "&buildTypeId=" + buildTypeId;
        BS.ajaxRequest(url, {
          onComplete: function() {
              document.location.reload();
          },
          onFailure: function() {
              alert('Unable to unlink dependency');
          }
        });
    }
};

BS.ResourceUI = {
    collapseAllBuildTypes : function() {
        jQuery('.buildConfigurationRow').hide();
        return false
    },

    expandAllBuildTypes : function() {
        jQuery('.buildConfigurationRow').show();
        return false
    }
};

BS.ResourceMonitor = {
    start:function (url) {
        this._updater = new BS.PeriodicalUpdater(null, url, {
            frequency:15,
            onSuccess:function (transport) {
                var doc = BS.Util.documentRoot(transport);
                var resources = doc.getElementsByTagName("resource");
                if (!resources || resources.length == 0) return;

                for (var i = 0; i < resources.length; i++) {
                    var id = resources[i].getAttribute("id");
                    var available = resources[i].getAttribute("available");
                    var container = $('resourceStatus_' + id);
                    if (container) {
                        container.innerHTML = (available == 'true') ? "Available" : "Unavailable";
                    }
                    var count = resources[i].getAttribute("count");
                    container = $('resourceUsage_' + id);
                    if (container) {
                        container.innerHTML = count;
                    }
                }
            }
        });
    }
}
