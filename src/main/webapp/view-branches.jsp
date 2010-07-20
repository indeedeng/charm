<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>CHARM SVN Release Branches</title>
    <style type="text/css"><jsp:include page="charm.css"/></style>
    <link rel="shortcut icon" href="/charm/favicon.ico"/>
</head>
<body>
<h2><s:property value="project"/> branches</h2>
<s:if test="trunkDifferent">
    <s:url var="trunkSinceBranchUrl" action="logTrunkSinceBranch">
        <s:param name="project" value="project"/>
    </s:url>
    <div class="message">trunk has <s:a href="%{trunkSinceBranchUrl}">changes since latest branch</s:a></div>
</s:if>
<table>
<s:iterator value="branches" var="branch">
    <s:if test="%{ivyEnabled}">
        <s:url var="depsUrl" action="showIvyDeps">
            <s:param name="project" value="project"/>
            <s:param name="branchDate" value="name"/>
        </s:url>
    </s:if>
    <s:url var="branchLogUrl" action="logBranch">
        <s:param name="project" value="project"/>
        <s:param name="branchDate" value="name"/>
    </s:url>
    <s:url var="logTrunkUrl" action="logTrunkSinceBranch">
        <s:param name="project" value="project"/>
        <s:param name="branchDate" value="name"/>
    </s:url>
    <tr>
        <td><s:property value="name"/></td>
        <td><s:property value="author"/></td>
        <td><s:property value="date"/></td>
        <s:if test="%{ivyEnabled}">
            <td><s:a href="%{depsUrl}">dependencies</s:a></td>
        </s:if>
        <td><s:a href="%{branchLogUrl}">branch log</s:a></td>
        <td><s:a href="%{logTrunkUrl}">changes on trunk</s:a></td>
    </tr>
</s:iterator>
</table>
</body>
<p></p><a href="/charm/">CHARM Home</a></p>
</html>
