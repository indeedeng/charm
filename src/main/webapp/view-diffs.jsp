<%@ taglib prefix="s" uri="/struts-tags" %>

<html>
<head>
    <title>CHARM SVN Diff Summary</title>
    <style type="text/css"><jsp:include page="charm.css"/></style>
    <link rel="shortcut icon" href="/charm/favicon.ico"/>
</head>
<body>
<h2><s:property value="path1"/> vs. <s:property value="path2"/></h2>
<p>
    <s:if test='%{branchDate != null}'>
        <s:url var="branchLogUrl" action="logBranch">
            <s:param name="project" value="project"/>
            <s:param name="branchDate" value="branchDate"/>
            <s:param name="path" value="path"/>
        </s:url>
        <span><s:a href="%{branchLogUrl}">branch log</s:a></span>
        <s:url var="trunkToBranchUrl" action="logTrunkSinceBranch">
            <s:param name="project" value="project"/>
            <s:param name="branchDate" value="branchDate"/>
        </s:url>
        <span><s:a href="%{trunkToBranchUrl}">changes on trunk</s:a></span>
    </s:if>
    <s:if test='%{tag != null}'>
        <s:url var="trunkToTagUrl" action="logTrunkSinceTag">
            <s:param name="project" value="project"/>
            <s:param name="tag" value="tag"/>
        </s:url>
        <span><s:a href="%{trunkToTagUrl}">changes on trunk</s:a></span>
    </s:if>
</p>
<table>
<s:iterator value="diffs">
    <s:if test='%{branchDate != null}'>
        <s:url var="branchLogUrl" action="logBranch">
            <s:param name="project" value="project"/>
            <s:param name="branchDate" value="branchDate"/>
            <s:param name="path" value="path"/>
        </s:url>
        <s:url var="trunkToBranchUrl" action="logTrunkSinceBranch">
            <s:param name="project" value="project"/>
            <s:param name="branchDate" value="branchDate"/>
            <s:param name="path" value="path"/>
        </s:url>
    </s:if>
    <s:if test='%{tag != null}'>
        <s:url var="trunkToTagLogUrl" action="logTrunkSinceTag">
            <s:param name="project" value="project"/>
            <s:param name="tag" value="tag"/>
            <s:param name="path" value="path"/>
        </s:url>
    </s:if>
    <tr>
        <td><s:property value="modificationType"/></td>
        <td><s:property value="kind"/></td>
        <td><s:property value="path"/></td>
        <s:if test='%{branchDate != null}'>
            <td><s:a href="%{branchLogUrl}">branch log</s:a></td>
            <td><s:a href="%{trunkToBranchLogUrl}">changes on trunk</s:a></td>
        </s:if>
        <s:if test='%{tag != null}'>
            <td><s:a href="%{trunkToTagLogUrl}">changes on trunk</s:a></td>
        </s:if>
    </tr>
</s:iterator>
</table>
<s:if test="%{branchDate != null}">
    <s:url var="projectUrl" action="listBranches">
        <s:param name="project" value="project"/>
    </s:url>
</s:if>
<s:elseif test="%{tag != null}">
    <s:url var="projectUrl" action="listTags">
        <s:param name="project" value="project"/>
    </s:url>
</s:elseif>
<p><s:a href="%{projectUrl}"><s:property value="project"/></s:a></p>
<p></p><a href="/charm/">CHARM Home</a></p>
</body>
</html>
