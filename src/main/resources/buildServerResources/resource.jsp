<%@ include file="/include.jsp" %>

<jsp:useBean id="resources" scope="request" type="java.util.List"/>

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
                            <td class="edit highlight">edit</td>
                            <td class="edit">delete</td>
                        </tr>
                    </tbody>
                </table>
                <br/>
            </c:forEach>
        </c:if>
    </div>
</div>
