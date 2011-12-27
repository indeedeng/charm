#!/bin/sh
# Requires Maven installed and in PATH
mvn install:install-file -DgroupId=com.indeed -DartifactId=common-jira -Dversion=0.1.2 -Dpackaging=jar -Dfile=common-jira-0.1.2.jar
mvn install:install-file -DgroupId=com.indeed -DartifactId=atlassian-ws -Dversion=0.1.4 -Dpackaging=jar -Dfile=atlassian-ws-0.1.4.jar
