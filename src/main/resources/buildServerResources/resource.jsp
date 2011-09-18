<%@ include file="/include.jsp" %>

<jsp:useBean id="resources" scope="request" type="java.util.List"/>
<jsp:useBean id="buildTypes" scope="request" type="java.util.Map"/>

<style type="text/css">
    @import "<c:url value='/css/forms.css'/>";
    @import "<c:url value='/css/admin/adminMain.css'/>";
    @import "<c:url value='/css/admin/projectConfig.css'/>";
</style>

<div id="container">
    <div class="editResourcesPage">
        <h3 class="title">Resources</h3>
        <c:if test="${not empty resources}">
            <c:forEach items="${resources}" var="resource">
                <table id="resource" class="resourceTable">
                    <tbody>
                        <tr class="resource">
                            <td class="name highlight">${resource.name}</td>
                            <td class="host highlight">${resource.host}</td>
                            <td class="port highlight">${resource.port}</td>
                            <td class="edit highlight"
                                onclick="BS.EditResourceDialog.showDialog('${resource.name}', '${resource.host}', '${resource.port}');"
                                title="Click to edit resource">
                                <a href="javascript://"
                                   onclick="BS.EditResourceDialog.showDialog('${resource.name}', '${resource.host}', '${resource.port}');">
                                    edit
                                </a>
                            </td>
                            <td class="edit">delete</td>
                        </tr>
                        <c:forEach items="${resource.buildTypes}" var="buildType">
                            <tr class="buildConfigurationRow">
                                <td class="buildConfiguration" colspan="4">
                                    <c:out value="${buildTypes[buildType].fullName}"/>
                                </td>
                                <td class="edit">delete</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                <br/>
            </c:forEach>
        </c:if>
    </div>
</div>

<p class="addNew">
  <a href="javascript://" onclick="BS.EditResourceDialog.showDialog('', '', '')">
    Create new resource
  </a>
</p>

<div id="editResourceDialog" class="editResourceDialog modalDialog">
    <div class="dialogHeader">
        <div class="closeWindow">
            <a title="Close dialog window" href="javascript://" onclick="BS.EditResourceDialog.cancelDialog()">close</a>
        </div>
        <h3 id="resourceDialogTitle" class="dialogTitle"></h3>
    </div>

    <div class="modalDialogBody">
        <form id="editResourceForm" action="<c:url value='/resource.html?'/>" method="post"
              onsubmit="return BS.EditResourceForm.saveResource();">
            <label class="resourceNameLabel" for="resourceName">Name: <l:star/></label>
            <forms:textField name="resourceName" maxlength="50" style="width: 22em;"/>
            <span class="error" id="error_resourceName" style="margin-left: 5.5em;"></span>

            <div class="clr" style="height:3px;"></div>
            <label for="resourceHost">Host:</label>
            <forms:textField name="resourceHost" maxlength="50" style="width: 22em;" onchange=""/>

            <div class="clr" style="height:3px;"></div>
            <label for="resourcePort">Port:</label>
            <forms:textField name="resourcePort" maxlength="5" style="width: 5em;" onchange=""/>

            <div class="popupSaveButtonsBlock">
                <a href="javascript://" onclick="BS.EditResourceDialog.cancelDialog()" class="cancel">Cancel</a>
                <input class="submitButton" type="submit" value="Save"/>
                <br clear="all"/>
            </div>

            <input type="hidden" name="submitAction" value=""/>
        </form>
    </div>
</div>
