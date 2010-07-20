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
    <s:url var="trunkSinceTagUrl" action="logTrunkSinceTag">
        <s:param name="project" value="project"/>
    </s:url>
    <div class="message">trunk has <s:a href="%{trunkSinceTagUrl}">changes since latest tag</s:a></div>
</s:if>
<table>
<s:iterator value="tags" var="tag">
    <s:url var="trunkSinceTagUrl" action="logTrunkSinceTag">
        <s:param name="project" value="project"/>
        <s:param name="tag" value="name"/>
    </s:url>
    <tr>
        <td><s:property value="name"/></td>
        <td><s:property value="author"/></td>
        <td><s:property value="date"/></td>
        <td><s:a href="%{trunkSinceTagUrl}">changes on trunk</s:a></td></td>
    </tr>
</s:iterator>
</table>
</body>
<p></p><a href="/charm/">CHARM Home</a></p>
</html>
