<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>CHARM Dependencies</title>
    <style type="text/css"><jsp:include page="charm.css"/></style>
    <link rel="shortcut icon" href="/charm/favicon.ico"/>
    <script type="text/javascript">
        <jsp:include page="ajax.js"/>
    </script>
</head>
<body>
<h2>
    <s:property value="project"/>
    <s:if test="%{branchDate != null}">
        <s:property value="branchDate"/>
    </s:if>
    <s:elseif test="%{tag != null}">
        <s:property value="tag"/>
    </s:elseif>
    <s:else>
        trunk
    </s:else>
        dependencies
</h2>
<p>
    view:
    <s:url var="logChangesUrl" action="logTrunkSinceBranch">
        <s:param name="project" value="project"/>
        <s:param name="branchDate" value="branchDate"/>
    </s:url>
    &nbsp;&nbsp;<a href="<s:property value="logChangesUrl" escape="false"/>">changes since branch</a>
    <s:url var="logBranchUrl" action="logBranch">
        <s:param name="project" value="project"/>
        <s:param name="branchDate" value="branchDate"/>
    </s:url>
    &nbsp;&nbsp;<a href="<s:property value="logBranchUrl" escape="false"/>">branch log</a>
</p>
<s:url var="analyzeUrl" action="listDependencyConflicts">
    <s:param name="project" value="project"/>
    <s:param name="branchDate" value="branchDate"/>
    <s:param name="tag" value="tag"/>
</s:url>
<s:a href="%{analyzeUrl}">analyze conflicts</s:a>
<s:if test="%{!showAll}">
    <s:url var="showAllUrl">
        <s:param name="project" value="project"/>
        <s:param name="branchDate" value="branchDate"/>
        <s:param name="tag" value="tag"/>
        <s:param name="showAll" value="true"/>
    </s:url>
    <br/><s:a href="%{showAllUrl}">show all orgs</s:a>
</s:if>
<s:url action="checkForTrunkDiffs" var="checkTrunkForDiffsUrl"/>
<script type="text/javascript">
    function showResult(result) {
        if ( result.readyState != 4 || result.responseText == '' ) { return; }

        eval('response = ' + result.responseText);
        var elt = document.getElementById(response.project + "__" + response.tag);
        if (response.trunkDifferent) {
            elt.innerHTML = "<b>trunk different</b>";
        } else {
            elt.innerHTML = "no changes on trunk";
        }
    }

    function checkTrunkForDiffs(project, tag) {
        sendRequest( '<s:property value="checkTrunkForDiffsUrl"/>?project=' + project + '&tag=' + tag , 'GET', showResult );
    }

    function checkAllForTrunkDiffs() {
        var elts = document.getElementsByTagName("span");
        for (var i = 0; i < elts.length; i++) {
            if (elts[i].className == 'changes') {
                elts[i].innerHTML = "<i>working...</i>";
                var pv = elts[i].id.split("__");
                checkTrunkForDiffs(pv[0], pv[1]);
            }
        }
        document.getElementById('checkAll').style.display = "none";
    }
</script>
<div id="checkAll"><a href="#" onclick="checkAllForTrunkDiffs(); return false;">check all for trunk changes</a></div>
<table>
    <tr>
        <th>Organization</th>
        <th>Name</th>
        <th>Revision</th>
        <th>&nbsp;</th>
        <th>Latest</th>
    </tr>
<s:iterator value="dependencies" var="dep">
    <s:if test="homeOrg">
        <tr>
            <td><s:property value="org"/></td>
            <s:url var="projectUrl" action="listTags">
                <s:param name="project" value="path"/>
            </s:url>
            <s:url var="depsUrl">
                <s:param name="project" value="name"/>
                <s:param name="tag" value="rev"/>
            </s:url>
            <s:url var="logUrl" action="logTrunkSinceTag">
                <s:param name="project" value="path"/>
                <s:param name="tag" value="rev"/>
            </s:url>
            <td><s:a href="%{projectUrl}"><s:property value="name"/></s:a></td>
            <td><s:a href="%{depsUrl}"><s:property value="rev"/></s:a></td>
            <td><s:a href="%{logUrl}"><span id='<s:property value="%{path}"/>__<s:property value="%{rev}"/>' class="changes">changes on trunk</span></s:a></td>
            <s:if test="%{rev == latestRev}">
                <td><s:property value="latestRev"/></td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
            </s:if>
            <s:else>
                <s:url var="depsUrl">
                    <s:param name="project" value="name"/>
                    <s:param name="tag" value="latestRev"/>
                </s:url>
                <td><b><s:a href="%{depsUrl}"><s:property value="latestRev"/></s:a></b></td>
                <s:url var="logLatestUrl" action="logTrunkSinceTag">
                    <s:param name="project" value="path"/>
                    <s:param name="tag" value="latestRev"/>
                </s:url>
                <td><s:a href="%{logLatestUrl}"><span id='<s:property value="%{path}"/>__<s:property value="%{latestRev}"/>' class="changes">changes on trunk</span></s:a></td>
                <s:if test="branchDate != null">
                    <s:url var="upgradeUrl" action="upgradeIvyDep">
                        <s:param name="project" value="project"/>
                        <s:param name="branchDate" value="branchDate"/>
                        <s:param name="module" value="name"/>
                        <s:param name="oldRev" value="rev"/>
                        <s:param name="newRev" value="latestRev"/>
                    </s:url>
                    <td><s:a href="%{upgradeUrl}">upgrade</s:a></td>
                </s:if>
            </s:else>
        </tr>
    </s:if>
    <s:elseif test="%{showAll}">
        <tr>
            <td><s:property value="org"/></td>
            <td><s:property value="name"/></td>
            <td><s:property value="rev"/></td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
        </tr>
    </s:elseif>
</s:iterator>
</table>
</body>
<p></p><a href="/charm/">CHARM Home</a></p>
</html>
