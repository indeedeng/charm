<%@ taglib prefix="s" uri="/struts-tags" %>
<s:url var="refreshUrl">
    <s:param name="project" value="project"/>
    <s:param name="branchDate" value="branchDate"/>
    <s:param name="revision" value="revision"/>
    <s:param name="user" value="user"/>
    <s:param name="jobId" value="job.id"/>
</s:url>
<s:url var="branchLogUrl" action="logBranch">
    <s:param name="project" value="project"/>
    <s:param name="branchDate" value="branchDate"/>
</s:url>
<s:url var="trunkToBranchUrl" action="logTrunkSinceBranch">
    <s:param name="project" value="project"/>
    <s:param name="branchDate" value="branchDate"/>
</s:url>
<html>
<head>
    <title>CHARM SVN Revision</title>
    <style type="text/css"><jsp:include page="charm.css"/></style>
    <link rel="shortcut icon" href="/charm/favicon.ico"/>
    <s:if test="job.running">
        <meta http-equiv="REFRESH" content="1; URL=<s:property value="refreshUrl" escape="false"/>">
    </s:if>
</head>
<body>
<h2><s:property value="job.title"/></h2>
<p><b>Status:</b> <s:property value="job.status"/></p>
<textarea rows="20" cols="120" style="font-family: courier; font-size: 11; "><s:property value="job.log"/></textarea>
<table>
<s:if test="job.running">
    <tr><td><s:a href="%{refreshUrl}">refresh</s:a></td></tr>
</s:if>
<s:url var="cleanupUrl" action="cleanupBranchWC">
    <s:param name="project" value="project"/>
    <s:param name="branchDate" value="branchDate"/>
    <s:param name="user" value="user"/>
</s:url>
<tr><td><s:a href="%{cleanupUrl}">clean up working directory</s:a></td></tr>
</table>
<table><tr>
    <td><s:a href="%{branchLogUrl}">branch log</s:a></td>
    <td><s:a href="%{trunkToBranchUrl}">changes on trunk</s:a></td>
</tr></table>
<p><a href="/charm/">CHARM Home</a></p>
</body>
</html>
