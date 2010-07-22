<%@ taglib prefix="s" uri="/struts-tags" %>
<s:url var="refreshUrl">
    <s:param name="project" value="project"/>
    <s:param name="format" value="format"/>
    <s:param name="branchDate" value="branchDate"/>
    <s:param name="tag" value="tag"/>
    <s:param name="module" value="module"/>
    <s:param name="rev" value="rev"/>
    <s:param name="jobId" value="job.id"/>
</s:url>
<html>
<head>
    <title>CHARM Conflict Analysis</title>
    <style type="text/css"><jsp:include page="charm.css"/></style>
    <link rel="shortcut icon" href="/charm/favicon.ico"/>
    <s:if test="job.running">
        <meta http-equiv="REFRESH" content="2; URL=<s:property value="refreshUrl" escape="false"/>">
    </s:if>
    <script type="text/javascript">
        function scrollTo(elt){
            var top = 0;
            if (elt.offsetParent) {
                do {
                    top += elt.offsetTop;
                } while (elt = elt.offsetParent);
                window.scroll(0, top - 30);
            }
        }
        function highlight(elt) {
            elt.style.backgroundColor = "#FFF8C6";
        }
        function unhighlight(elt) {
            elt.style.backgroundColor = "";
        }
        function highlightRow(cur, id) {
            var row = document.getElementById(id);
            if (row != null) {
                unhighlight(cur);
                highlight(row);
                scrollTo(row);
                setTimeout("unhighlight(document.getElementById('" + id + "'));", 1000);
            }
        }
    </script>
</head>
<body>
<h2><s:property value="job.title"/></h2>
<s:if test="job.running">
    <p>
        <b>Status:</b> <s:property value="job.status"/>
    </p>
    <pre style="padding-left: 2em; font-family: courier; font-size: 11; "><s:property value="job.log"/></pre>
    <p style="padding-left: 1em;">
        <img src="/charm/wait20.gif" alt="job is running"/>
    </p>
</s:if>
<s:else>
    <s:if test="conflictRecords.size > 0">
    <h3>Conflicts</h3>
    <table>
        <tr>
            <th>Org</th>
            <th>Library</th>
            <th>Rev</th>
            <th>Depended on by...</th>
        </tr>
        <s:iterator value="conflictRecords" var="record">
            <s:url var="listTagsUrl" action="listTags">
                <s:param name="project" value="path"/>
            </s:url>
            <s:url var="listDepsUrl" action="showIvyDeps">
                <s:param name="project" value="path"/>
                <s:param name="tag" value="rev"/>
            </s:url>
            <tr id="<s:property/>" >
                <td><s:property value="org"/></td>
                <s:if test="homeOrg == org">
                    <td style="white-space: nowrap;"><s:a target="_blank" href="%{listTagsUrl}"><s:property value="name"/></s:a></td>
                    <td style="white-space: nowrap;"><s:a target="_blank" href="%{listDepsUrl}"><s:property value="rev"/></s:a></td>
                </s:if>
                <s:else>
                    <td style="white-space: nowrap;"><s:property value="org"/>/<s:property value="name"/></td>
                    <td style="white-space: nowrap;"><s:property value="rev"/></td>
                </s:else>
                <td>
                    <s:iterator value="dependents">
                        <s:url var="listTagsUrl" action="listTags">
                            <s:param name="project" value="path"/>
                            <s:param name="tag" value="rev"/>
                        </s:url>
                        <span style="white-space: nowrap;" onmouseover="highlight(this)" onmouseout="unhighlight(this)" onclick="highlightRow(this,'<s:property/>');" >
                            <s:property value="name"/>
                            <s:property value="rev"/>,</span>
                    </s:iterator>
                </td>
            </tr>
        </s:iterator>
    </table>
    </s:if>
    <s:else>
        <h3>No Conflicts Found</h3>
    </s:else>
    <s:if test="nonConflictRecords.size > 0">
    <h3>Non-Conflicts</h3>
    <table>
        <s:iterator value="nonConflictRecords" var="record">
            <s:url var="listTagsUrl" action="listTags">
                <s:param name="project" value="path"/>
            </s:url>
            <s:url var="listDepsUrl" action="showIvyDeps">
                <s:param name="project" value="path"/>
                <s:param name="tag" value="rev"/>
            </s:url>
            <tr id="<s:property/>" >
                <td><s:property value="org"/></td>
                <s:if test="homeOrg == org">
                    <td style="white-space: nowrap;"><s:a target="_blank" href="%{listTagsUrl}"><s:property value="name"/></s:a></td>
                    <td style="white-space: nowrap;"><s:a target="_blank" href="%{listDepsUrl}"><s:property value="rev"/></s:a></td>
                </s:if>
                <s:else>
                    <td style="white-space: nowrap;"><s:property value="org"/>/<s:property value="name"/></td>
                    <td style="white-space: nowrap;"><s:property value="rev"/></td>
                </s:else>
                <td>
                    <s:iterator value="dependents">
                        <s:url var="listTagsUrl" action="listTags">
                            <s:param name="project" value="path"/>
                            <s:param name="tag" value="rev"/>
                        </s:url>
                        <span style="white-space: nowrap;" onmouseover="highlight(this)" onmouseout="unhighlight(this)" onclick="highlightRow(this,'<s:property/>');" >
                            <s:property value="name"/>
                            <s:property value="rev"/>,</span>
                    </s:iterator>
                </td>
            </tr>
        </s:iterator>
    </table>
    </s:if>
    <p>
        <b>Status:</b> <s:property value="job.status"/>
    </p>
    <pre style="padding-left: 2em; font-family: courier; font-size: 11; "><s:property value="job.log"/></pre>
</s:else>

<p><a href="/charm/">CHARM Home</a></p>
</body>
</html>
