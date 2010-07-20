<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>CHARM SVN Published Version Tags</title>
    <style type="text/css"><jsp:include page="charm.css"/></style>
    <link rel="shortcut icon" href="/charm/favicon.ico"/>
</head>
<body>
<h2><s:property value="project"/> tags</h2>
<s:if test="trunkDifferent">
    <s:url var="trunkDiffUrl" action="diffTagToTrunk">
        <s:param name="project" value="project"/>
    </s:url>
    <div class="message">trunk has <s:a href="%{trunkDiffUrl}">differences from latest tag</s:a></div>
</s:if>
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
