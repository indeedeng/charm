package com.indeed.charm.ivy;

import com.indeed.charm.ReleaseEnvironment;
import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;
import com.indeed.charm.svn.SubversionClient;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.Properties;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

/**
 */
public class IvyLoader {
    private static Logger log = Logger.getLogger(IvyLoader.class);

    private final VCSClient vcsClient;
    private final String project;
    private final String subPath; // branch/tag/trunk
    private final String ivyOrg;
    private final String ivyFileName;
    private final String ivyProperties;

    public IvyLoader(VCSClient vcsClient, String project, String subPath, String ivyOrg, String ivyFileName, String ivyProperties) {
        this.vcsClient = vcsClient;
        this.project = project;
        this.subPath = subPath;
        this.ivyOrg = ivyOrg;
        this.ivyFileName = ivyFileName;
        this.ivyProperties = ivyProperties;
    }

    protected Properties loadProperties() {
        Properties properties = new Properties();
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            vcsClient.getFile(ivyProperties, -1, outputStream);
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            properties.load(inputStream);
        } catch (VCSException e) {
            log.error("failed to load ivy properties", e);
        } catch (IOException e) {
            log.error("failed to load ivy properties", e);
        }
        return properties;
    }

    private static final Pattern propPattern = Pattern.compile("\\$\\{([^\\}]+)\\}");

    protected String resolve(Properties properties, String value) {
        Matcher m = propPattern.matcher(value);
        while (m.find()) {
            final String matched = m.group(0);
            final String prop = m.group(1);
            String propVal = properties.getProperty(prop, null);
            if (propVal != null) {
                value = value.replace(matched, propVal);
            }
        }
        return value;
    }

    protected String getPath(String name) {
        try {
            if (!vcsClient.checkExistsInHead(name)) {
                String altName = name.replaceAll("-", "/");
                if (vcsClient.checkExistsInHead(altName)) {
                    name = altName;
                } else {
                    log.error(name + " is not a valid project");
                }
            }
        } catch (VCSException e) {
            log.error("Can't get path for " + name, e);
        }
        return name;
    }

    protected String getLatestRev(String path) {
        try {
            return vcsClient.listTags(path, 1, VCSClient.Ordering.REVERSE_AGE).get(0).getName();
        } catch (VCSException e) {
            log.error("Can't get latest rev for " + path, e);
        }
        return "";
    }

    public List<IvyDependency> loadDependencies() {
        List<IvyDependency> deps = Lists.newArrayList();

        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            vcsClient.getFile(project + subPath + ivyFileName, -1, outputStream);
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            Properties properties = loadProperties();

            final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document doc = docBuilder.parse(inputStream);
            final NodeList nodeList = doc.getElementsByTagName("dependency");
            for (int i = 0; i < nodeList.getLength(); i++) {
                final Node node = nodeList.item(i);
                final NamedNodeMap attrs = node.getAttributes();
                final String org = attrs.getNamedItem("org").getNodeValue();
                final String name = attrs.getNamedItem("name").getNodeValue();
                final String rev = resolve(properties, attrs.getNamedItem("rev").getNodeValue());
                final boolean homeOrg = ivyOrg.equals(org);
                final String path = homeOrg ? getPath(name) : "";
                final String latestRev = homeOrg ? getLatestRev(path) : "";
                deps.add(new IvyDependency(org, name, path, rev, homeOrg, latestRev));
            }
        } catch (ParserConfigurationException e) {
            log.error("failed to parse ivy xml", e);
        } catch (VCSException e) {
            log.warn("failed to load ivy xml: " + e.getMessage());
        } catch (SAXException e) {
            log.error("failed to parse ivy xml", e);
        } catch (IOException e) {
            log.error("failed to parse ivy xml", e);
        }

        Collections.sort(deps);
        return ImmutableList.copyOf(deps);
    }

    public static void main(String[] args) throws Exception {
        ReleaseEnvironment env = new ReleaseEnvironment();
        VCSClient cli = new SubversionClient(env);
        IvyLoader loader = new IvyLoader(cli, args[0], args[1], env.getIvyOrg(), env.getIvyFileName(), env.getIvyProperties());
        System.out.println(loader.loadDependencies());
    }
}
