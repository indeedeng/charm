<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>CHARM SVN PublishedTags</title>
    <style type="text/css"><jsp:include page="charm.css"/></style>
    <link rel="shortcut icon" href="/charm/favicon.ico"/>
</head>
<body>
<h2><s:property value="project"/> tags</h2>
<table>
<s:iterator value="tags" var="tag">
    <s:url var="trunkDiffUrl" action="diffTagToTrunk">
        <s:param name="project" value="project"/>
        <s:param name="tag" value="tag"/>
    </s:url>
    <s:url var="logTrunkUrl" action="logTrunkSinceTag">
        <s:param name="project" value="project"/>
        <s:param name="tag" value="tag"/>
    </s:url>
    <tr>
        <td><s:property/></td>
        <td><s:a href="%{trunkDiffUrl}">diff trunk</s:a></td>
        <td><s:a href="%{logTrunkUrl}">changes on trunk</s:a></td>
    </tr>
</s:iterator>
</table>
</body>
<p></p><a href="/charm/">CHARM Home</a></p>
</html>
