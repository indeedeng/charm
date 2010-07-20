<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>CHARM SVN Projects</title>
    <style type="text/css"><jsp:include page="charm.css"/></style>
    <link rel="shortcut icon" href="/charm/favicon.ico"/>
</head>
<body>
<h2>Release Projects</h2>
<table>
    <s:iterator value="releaseProjects" var="project">
        <s:if test="%{ivyEnabled}">
            <s:url var="depsUrl" action="showIvyDeps">
                <s:param name="project" value="#project.name"/>
            </s:url>
        </s:if>
        <s:url var="branchesUrl" action="listBranches">
            <s:param name="project" value="#project.name"/>
        </s:url>
        <s:url var="diffBranchToTrunkUrl" action="diffBranchToTrunk">
            <s:param name="project" value="#project.name"/>
        </s:url>
        <s:if test="publishedTags">
            <s:url var="tagsUrl" action="listTags">
                <s:param name="project" value="#project.name"/>
            </s:url>
            <s:url var="diffTagToTrunkUrl" action="diffTagToTrunk">
                <s:param name="project" value="#project.name"/>
            </s:url>
        </s:if>
        <tr>
            <td><s:property value="name"/></td>
            <s:if test="%{ivyEnabled}">
                <td><s:a href="%{depsUrl}">dependencies</s:a></td>
            </s:if>
            <td><s:a href="%{branchesUrl}">branches</s:a></td>
            <td><s:a href="%{diffBranchToTrunkUrl}">latest branch vs. trunk</s:a></td>
            <s:if test="publishedTags">
                <td><s:a href="%{tagsUrl}">tags</s:a></td>
                <td><s:a href="%{diffTagToTrunkUrl}">latest tag vs. trunk</s:a></td>
            </s:if>
            <s:else><td>&nbsp;</td><td>&nbsp;</td></s:else>
        </tr>
    </s:iterator>
</table>
<h2>Library Projects</h2>
<table>
    <s:iterator value="libraryProjects" var="project">
        <s:if test="%{ivyEnabled}">
            <s:url var="depsUrl" action="showIvyDeps">
                <s:param name="project" value="#project.name"/>
            </s:url>
        </s:if>
        <s:url var="tagsUrl" action="listTags">
            <s:param name="project" value="#project.name"/>
        </s:url>
        <s:url var="diffTagToTrunkUrl" action="diffTagToTrunk">
            <s:param name="project" value="#project.name"/>
        </s:url>
        <tr>
            <td><s:property value="name"/></td>
            <s:if test="%{ivyEnabled}">
                <td><s:a href="%{depsUrl}">dependencies</s:a></td>
            </s:if>
            <td><s:a href="%{tagsUrl}">tags</s:a></td>
            <td><s:a href="%{diffTagToTrunkUrl}">latest tag vs. trunk</s:a></td>
        </tr>
    </s:iterator>
</table>
</body>
</html>