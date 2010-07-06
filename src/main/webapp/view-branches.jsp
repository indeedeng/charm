<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>CHARM SVN Release Branches</title>
    <style type="text/css"><jsp:include page="charm.css"/></style>
    <link rel="shortcut icon" href="/charm/favicon.ico"/>
</head>
<body>
<h2><s:property value="project"/> branches</h2>
<table>
<s:iterator value="branches" var="branchDate">
    <s:url var="branchLogUrl" action="logBranch">
        <s:param name="project" value="project"/>
        <s:param name="branchDate" value="branchDate"/>
    </s:url>
    <s:url var="trunkDiffUrl" action="diffBranchToTrunk">
        <s:param name="project" value="project"/>
        <s:param name="branchDate" value="branchDate"/>
    </s:url>
    <s:url var="logTrunkUrl" action="logTrunkSinceBranch">
        <s:param name="project" value="project"/>
        <s:param name="branchDate" value="branchDate"/>
    </s:url>
    <tr>
        <td><s:property/></td>
        <td><s:a href="%{branchLogUrl}">branch log</s:a></td>
        <td><s:a href="%{trunkDiffUrl}">diff trunk</s:a></td>
        <td><s:a href="%{logTrunkUrl}">changes on trunk</s:a></td>
    </tr>
</s:iterator>
</table>
</body>
<p></p><a href="/charm/">CHARM Home</a></p>
</html>
