<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>CHARM Dependencies</title>
    <style type="text/css"><jsp:include page="charm.css"/></style>
    <link rel="shortcut icon" href="/charm/favicon.ico"/>
</head>
<body>
<h2><s:property value="project"/> dependencies</h2>
<s:if test="%{branchDate != null}">
    <h2><s:property value="branchDate"/></h2>
</s:if>
<s:else>
    <h3>trunk</h3>
</s:else>
<s:if test="%{!showAll}">
    <s:url var="showAllUrl">
        <s:param name="project" value="project"/>
        <s:param name="branchDate" value="branchDate"/>
        <s:param name="showAll" value="true"/>
    </s:url>
    <s:a href="%{showAllUrl}">show all orgs</s:a>
</s:if>
<table>
    <tr>
        <th>Organization</th>
        <th>Name</th>
        <th>Revision</th>
        <th>&nbsp;</th>
        <th>Latest</th>
    </tr>
<s:iterator value="dependencies" var="dep">
    <s:if test="homeOrg">
        <tr>
            <td><s:property value="org"/></td>
            <s:url var="projectUrl" action="listTags">
                <s:param name="project" value="path"/>
            </s:url>
            <s:url var="diffUrl" action="diffTagToTrunk">
                <s:param name="project" value="path"/>
                <s:param name="tag" value="rev"/>
            </s:url>
            <td><s:a href="%{projectUrl}"><s:property value="name"/></s:a></td>
            <td><s:property value="rev"/></td>
            <td><s:a href="%{diffUrl}">compare to trunk</s:a></td>
            <s:if test="%{rev == latestRev}">
                <td><s:property value="latestRev"/></td>
                <td>&nbsp;</td>
            </s:if>
            <s:else>
                <td><b><s:property value="latestRev"/></b></td>
                <s:url var="diffLatestUrl" action="diffTagToTag">
                    <s:param name="project" value="path"/>
                    <s:param name="tag1" value="rev"/>
                    <s:param name="tag2" value="latestRev"/>
                </s:url>
                <td><s:a href="%{diffLatestUrl}">compare <s:property value="rev"/> to <s:property value="latestRev"/></s:a></td>
            </s:else>
        </tr>
    </s:if>
    <s:elseif test="%{showAll}">
        <tr>
            <td><s:property value="org"/></td>
            <td><s:property value="name"/></td>
            <td><s:property value="rev"/></td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
        </tr>
    </s:elseif>
</s:iterator>
</table>
</body>
<p></p><a href="/charm/">CHARM Home</a></p>
</html>
