<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>CHARM SVN Login</title>
    <style type="text/css"><jsp:include page="charm.css"/></style>
    <link rel="shortcut icon" href="/charm/favicon.ico"/>
</head>
<body>
<h2>Upgrade ivy dependency on branch</h2>
<s:url var="checkUrl" action="checkUpgradeConflicts">
    <s:param name="project" value="project"/>
    <s:param name="branchDate" value="branchDate"/>
    <s:param name="module" value="module"/>
    <s:param name="rev" value="newRev"/>
</s:url>
<p><s:a href="%{checkUrl}">check for upgrade conflicts</s:a></p>
<s:form action="upgradeIvyDep" method="POST">
    <s:textfield name="user" label="User"/>
    <s:password name="password" label="Password"/>
    <s:textfield name="module" label="Module" value="%{module}"/>
    <s:textfield name="oldRev" label="From" value="%{oldRev}"/>
    <s:textfield name="newRev" label="To" value="%{newRev}"/>
    <s:textarea name="messagePrefix" label="Commit Message Prefix" value="%{messagePrefix}"/>
    <s:hidden name="project" value="%{project}"/>
    <s:hidden name="branchDate" value="%{branchDate}"/>
    <s:submit/>
</s:form>
<p><a href="/charm/">CHARM Home</a></p>
</body>
</html>
