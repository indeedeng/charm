<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>CHARM SVN Login</title>
    <style type="text/css"><jsp:include page="charm.css"/></style>
    <link rel="shortcut icon" href="/charm/favicon.ico"/>
</head>
<body>
<h2>SVN User/Password Required For Merge</h2>
<s:form action="mergeToBranch" method="POST">
    <s:textfield name="user" label="User"/>
    <s:password name="password" label="Password"/>
    <s:hidden name="project" value="%{project}"/>
    <s:hidden name="branchDate" value="%{branchDate}"/>
    <s:hidden name="revision" value="%{revision}"/>
    <s:submit/>
</s:form>
<p><a href="/charm/">CHARM Home</a></p>
</body>
</html>
