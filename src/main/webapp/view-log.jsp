<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>CHARM SVN Log</title>
    <style type="text/css"><jsp:include page="charm.css"/></style>
    <link rel="shortcut icon" href="/charm/favicon.ico"/>
    <script type="text/javascript" src="jquery-1.7.1.min.js"></script>
    <script type="text/javascript" src="jquery.ba-bbq.min.js"></script>
</head>
<body onload="addFixVersionFilters()">
<script type="text/javascript">
    function formatDate(time) {
        var d = new Date();
        d.setTime(time);
        var year = 1900 + d.getYear();
        var month = (d.getMonth() < 9 ? "0" : "") + (d.getMonth() + 1);
        var date = (d.getDate() < 10 ? "0" : "") + d.getDate();
        var hour = (d.getHours() < 10 ? "0" : "") + d.getHours();
        var minute = (d.getMinutes() < 10 ? "0" : "") + d.getMinutes();
        return year + "-" + month + "-" + date + " " + hour + ":" + minute;
    }

    function handleFixVersionHiding() {
        $.each($('td.fixVersion'), function(index, element) {
            var anchors = $(this).find('a');
            var hidden = true;
            if (anchors.size() == 0) {
                var tog = document.getElementById('nofixversion');
                if (tog && tog.checked) {
                    hidden = false;
                }
            } else {
                $.each(anchors, function(i, e) {
                    var tog = document.getElementById(e.innerHTML);
                    if (tog && tog.checked) {
                        hidden = false;
                        return false; // break out
                    }
                });
            }
            if (hidden) {
                $(this).parentsUntil('tbody').hide();
            } else {
                $(this).parentsUntil('tbody').show();
            }
        });
        updateFragment();
    }

    function addFixVersionFilters() {
        var filterObj = new Object();
        $.each($('td.fixVersion'), function(index, element) {
            var anchors = $(this).find('a');
            $.each(anchors, function(i, e) {
                filterObj[e.innerHTML] = 1;
            });
        });
        var filterSet = new Array();
        for (var filter in filterObj) {
            filterSet.push(filter);
        }
        var hidden = $.deparam.fragment().h;
        if (hidden && $.inArray("nofixversion", hidden) > -1) {
            document.getElementById("nofixversion").checked = false;
        }
        for (var index in filterSet.sort()) {
            var fv = filterSet[index];
            var checked = (hidden ? $.inArray(fv, hidden) == -1 : true);
            $('#filters').append(
                    "<span style='display: inline-block;'>" +
                    "<input type='checkbox' class='tgl' id='" + fv + "' " +
                    (checked ? "checked='true'" : "") +
                    "onclick='handleFixVersionHiding()'/>" +
                    "<label for='" + fv + "'>" + fv + "</label>&nbsp;</span>");
        }
        handleFixVersionHiding();
    }

    function updateFragment() {
        var hash = new Object();
        hash.h = new Array();
        $.each($('input.tgl'), function(i, e) {
            if (!e.checked) {
                hash.h.push(e.id);
            }
        });
        location.hash = $.param(hash);
    }
</script>
<h2><s:property value="name"/> since <s:property value="since"/></h2>
<s:if test='path != "."'>
    <h2><s:property value="path"/></h2>
</s:if>
<s:if test="showMergeToBranchLink">
    <s:url action="logBranch" var="logBranchUrl">
        <s:param name="project" value="project"/>
        <s:param name="branchDate" value="branchDate"/>
    </s:url>
    <p>
        <s:a href="%{logBranchUrl}">view branch log</s:a>
        <s:if test="branchDeployLink != null">
            | <a href="<s:property value="branchDeployLink" escape="false"/>" target="_new">deploy &#8663;</a>
        </s:if>
    </p>
</s:if>
<s:if test="%{foundAdditionalFields.contains('Fix Version')}">
    <p id="filters">
        Filters: <span style="display: inline-block;"><input type="checkbox" class='tgl' id="nofixversion" checked="true" onclick="handleFixVersionHiding()"/><label for="nofixversion">No Fix Version</label>&nbsp;</span>
    </p>
</s:if>
<table><tbody>
    <tr>
        <th>Date</th>
        <th>Revision</th>
        <th>Author</th>
        <th>Log Message</th>
        <s:if test="%{foundAdditionalFields.contains('Fix Version')}">
            <th nowrap>Fix Version(s)</th>
        </s:if>
        <s:if test="showMergeToBranchLink">
            <th>Merge Status</th>
        </s:if>
    </tr>
<s:iterator value="logEntries" var="logEntry">
    <s:if test="%{warnRevisions.contains(revision)}">
        <tr style="background-color: #FFFF66">
    </s:if>
    <s:else>
        <tr>
    </s:else>
        <td style="white-space: nowrap;"><script>document.write(formatDate(<s:property value="date.time"/>))</script></td>
        <td><s:a target="_blank" href="%{revisionUrl}"><s:property value="revision"/></s:a></td>
        <td><s:property value="author"/></td>
        <td><s:property value="logMessage" escape="false"/></td>
        <s:if test="%{foundAdditionalFields.contains('Fix Version')}">
            <td class="fixVersion">
                <s:if test="%{additionalFields.containsKey('Fix Version')}">
                    <s:property value="additionalFields['Fix Version']" escape="false"/>
                </s:if>
                <s:else>
                    <span style='display:none' class='nofixversion'>&nbsp;</span>
                </s:else>
            </td>
        </s:if>
        <s:if test="showMergeToBranchLink">
            <td style="white-space: nowrap;">
            <s:if test="branchMergeRevisions.size == 0">
                <s:url var="mergeUrl" action="mergeToBranch">
                    <s:param name="project" value="project"/>
                    <s:param name="branchDate" value="branchDate"/>
                    <s:param name="revision" value="revision"/>
                </s:url>
                <s:a href="%{mergeUrl}">merge to branch &#8663;</s:a>
            </s:if>
            <s:else>
                merged:
                <s:iterator value="branchMergeRevisions" var="mergeRev">
                    <s:a target="_blank" href="%{revisionUrl}"><s:property value="revision"/></s:a>&nbsp;
                </s:iterator>
            </s:else>
            </td>
        </s:if>
    </tr>
</s:iterator>
</tbody></table>
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
<p><a href="/charm/">CHARM Home</a></p>
</body>
</html>
