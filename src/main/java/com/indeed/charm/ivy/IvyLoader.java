package com.indeed.charm.ivy;

import com.indeed.charm.ReleaseEnvironment;
import com.indeed.charm.VCSClient;
import com.indeed.charm.VCSException;
import com.indeed.charm.svn.SubversionClient;
import com.google.common.collect.*;
import com.google.common.base.Supplier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

/**
 */
public class IvyLoader {
    private static Logger log = Logger.getLogger(IvyLoader.class);

    public static class BranchResolver implements Supplier<InputStream> {
        private final VCSClient vcsClient;
        private final ReleaseEnvironment env;
        private final String project;
        private final String branchDate;

        public BranchResolver(String project, String branchDate, VCSClient vcsClient, ReleaseEnvironment env) {
            this.project = project;
            this.branchDate = branchDate;
            this.vcsClient = vcsClient;
            this.env = env;
        }

        public InputStream get() {
            try {
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                vcsClient.getFile(project + env.getBranchPath() + branchDate + "/" + env.getIvyFileName(), -1, outputStream);
                return new ByteArrayInputStream(outputStream.toByteArray());
            } catch (VCSException e) {
                log.error("Failed to get " + project + " " + branchDate + " ivy content", e);
                return null;
            }
        }
    }

    public static class TrunkResolver implements Supplier<InputStream> {
        private final VCSClient vcsClient;
        private final ReleaseEnvironment env;
        private final String project;

        public TrunkResolver(String project, VCSClient vcsClient, ReleaseEnvironment env) {
            this.project = project;
            this.vcsClient = vcsClient;
            this.env = env;
        }

        public InputStream get() {
            try {
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                vcsClient.getFile(project + env.getTrunkPath() + "/" + env.getIvyFileName(), -1, outputStream);
                return new ByteArrayInputStream(outputStream.toByteArray());
            } catch (VCSException e) {
                log.error("Failed to get " + project + " trunk ivy content", e);
                return null;
            }
        }
    }

    public static class IvyReleaseResolver implements Supplier<InputStream> {
        private final String project;
        private final String tag;
        private final ReleaseEnvironment env;

        public IvyReleaseResolver(String project, String tag, ReleaseEnvironment env) {
            this.project = project;
            this.tag = tag;
            this.env = env;
        }

        public InputStream get() {
            try {
                URL ivyUrl = new URL(env.getIvyRepoUrl() + env.getIvyOrg() + '/' + project + "/ivy-" + tag + ".xml");
                URLConnection conn = ivyUrl.openConnection();
                return conn.getInputStream();
            } catch (IOException e) {
                log.error("Failed to get " + project + " " + tag + " ivy content", e);
                return null;
            }
        }
    }

    private final VCSClient vcsClient;
    private final ReleaseEnvironment env;

    private boolean findLatestRevs = true;

    public IvyLoader(VCSClient vcsClient, ReleaseEnvironment env) {
        this.vcsClient = vcsClient;
        this.env = env;
    }

    public boolean isFindLatestRevs() {
        return findLatestRevs;
    }

    public void setFindLatestRevs(boolean findLatestRevs) {
        this.findLatestRevs = findLatestRevs;
    }

    protected Properties loadProperties() {
        Properties properties = new Properties();
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            vcsClient.getFile(env.getIvyProperties(), -1, outputStream);
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

    protected static String getPath(ReleaseEnvironment env, VCSClient vcsClient, String name) {
        String path = env.getRepoForName(name);
        if (path != null) {
            return path;
        }
        try {
            if (!vcsClient.checkExistsInHead(name)) {
                String altName = name.replaceAll("-", "/");
                if (vcsClient.checkExistsInHead(altName)) {
                    name = altName;
                    env.putRepoName(altName, name);
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
        // TODO: way too slow, find a way to cache or speed this up
        try {
            return vcsClient.listTags(path, 1, VCSClient.Ordering.REVERSE_AGE).get(0).getName();
        } catch (VCSException e) {
            log.error("Can't get latest rev for " + path, e);
        }
        return "";
    }

    public List<IvyDependency> loadDependencies(Supplier<InputStream> resolver) {
        List<IvyDependency> deps = Lists.newArrayList();

        try {
            final InputStream inputStream = resolver.get();
            if (inputStream != null) {
                final Properties properties = loadProperties();
                final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                final Document doc = docBuilder.parse(inputStream);
                final NodeList nodeList = doc.getElementsByTagName("dependency");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    final Node node = nodeList.item(i);
                    final NamedNodeMap attrs = node.getAttributes();
                    final String org = attrs.getNamedItem("org").getNodeValue();
                    final String name = attrs.getNamedItem("name").getNodeValue();
                    final String rev = resolve(properties, attrs.getNamedItem("rev").getNodeValue());
                    final boolean homeOrg = env.getIvyOrg().equals(org);
                    final String path = homeOrg ? getPath(env, vcsClient, name) : "";
                    final String latestRev = findLatestRevs && homeOrg ? getLatestRev(path) : "";
                    deps.add(new IvyDependency(org, name, path, rev, homeOrg, latestRev));
                }
            }
        } catch (ParserConfigurationException e) {
            log.error("failed to parse ivy xml", e);
        } catch (SAXException e) {
            log.error("failed to parse ivy xml", e);
        } catch (IOException e) {
            log.error("failed to parse ivy xml", e);
        }

        Collections.sort(deps);
        return ImmutableList.copyOf(deps);
    }

    public static final class DepGraph {
        private final VCSClient client;
        private final ReleaseEnvironment env;
        private final DependencyNode root;
        private final Supplier<InputStream> rootResolver;
        private final Map<DependencyNode, DependencyNode> allDeps;
        private final Multimap<String, DependencyNode> byName;
        private final boolean findLatestRevs;

        public DepGraph(VCSClient client, ReleaseEnvironment env, String project, boolean isTag, String branchDateOrTag) {
            this.client = client;
            this.env = env;
            if (isTag) {
                this.root = new DependencyNode(env.getIvyOrg(), project, project, branchDateOrTag);
                this.rootResolver = new IvyReleaseResolver(project, branchDateOrTag, env);
                this.findLatestRevs = false;
            } else if (branchDateOrTag != null && !"trunk".equals(branchDateOrTag)) {
                this.root = new DependencyNode(env.getIvyOrg(), project, getPath(env, client, project), branchDateOrTag);
                this.rootResolver = new BranchResolver(project, branchDateOrTag, client, env);
                this.findLatestRevs = false;
            } else {
                this.root = new DependencyNode(env.getIvyOrg(), project, getPath(env, client, project), "trunk");
                this.rootResolver = new TrunkResolver(project, client, env);
                this.findLatestRevs = true;
            }
            this.allDeps = Maps.newHashMap();
            this.byName = HashMultimap.create();
        }

        public void build(Appendable status) throws IOException {
            status.append(".");
            log.info("Loading dependencies for " + root);
            IvyLoader loader = new IvyLoader(client, env);
            loader.setFindLatestRevs(findLatestRevs);
            List<IvyDependency> deps = loader.loadDependencies(rootResolver);
            for (IvyDependency dep : deps) {
                add(client, env, status, root, new DependencyNode(dep, env), dep.isHomeOrg());
            }

            status.append("\nloaded " + allDeps.size() + " dependencies\n");
        }

        private void add(VCSClient client, ReleaseEnvironment env, Appendable status, DependencyNode from, DependencyNode libVersion, boolean descend) throws IOException {
            Set<DependencyNode> deps = null;
            final DependencyNode existing = allDeps.get(libVersion);
            if (existing == null) {
                allDeps.put(libVersion, libVersion);
                byName.put(libVersion.getName(), libVersion);
            } else {
                libVersion = existing;
                deps = libVersion.getDependencies();
            }
            from.addDependency(libVersion);
            if (descend) {
                if (deps == null) {
                    status.append(".");
                    log.info("Loading dependencies for " + libVersion);
                    IvyLoader loader = new IvyLoader(client, env);
                    loader.setFindLatestRevs(findLatestRevs);
                    IvyReleaseResolver resolver = new IvyReleaseResolver(libVersion.getName(), libVersion.getRev(), env);
                    List<IvyDependency> ivyDeps = loader.loadDependencies(resolver);
                    deps = Sets.newHashSet();
                    for (IvyDependency dep : ivyDeps) {
                        if (dep.isHomeOrg()) {
                            deps.add(new DependencyNode(dep, env));
                        }
                    }
                }
                final String homeOrg = env.getIvyOrg();
                for (DependencyNode lv : deps) {
                    add(client, env, status, libVersion, lv, homeOrg.equals(lv.getOrg()));
                }
            }
        }

        public Collection<DependencyNode> findByName(String name) {
            return byName.get(name);
        }

        public List<DependencyNode> findConflicts(String name, String rev) {
            List<DependencyNode> conflicts = Lists.newLinkedList();
            Collection<DependencyNode> others = findByName(name);
            for (DependencyNode other : others) {
                if (!rev.equals(other.getRev())) {
                    if (other.getDependents().size() > 1 ||
                            !other.getDependents().contains(root)) {
                        conflicts.add(other);
                    }
                }
            }
            sortConflicts(conflicts);
            return conflicts;
        }

        public List<DependencyNode> listAllConflicts() {
            List<DependencyNode> all = Lists.newLinkedList();
            for (String name : byName.keySet()) {
                Collection<DependencyNode> libs = byName.get(name);
                if (libs.size() > 1) {
                    all.addAll(libs);
                }
            }
            sortConflicts(all);

            return all;
        }

        private void sortConflicts(List<DependencyNode> all) {
            // sort by name (ascending) then revision (descending)
            Collections.sort(all, new Comparator<DependencyNode>() {
                public int compare(DependencyNode o1, DependencyNode o2) {
                    int c = o1.org.compareTo(o2.org);
                    if (c == 0) {
                        c = o1.name.compareTo(o2.name);
                    }
                    if (c == 0) {
                        c = ReleaseEnvironment.normalizeVersion(o2.rev, 4).compareTo(
                                ReleaseEnvironment.normalizeVersion(o1.rev, 4));
                    }
                    return c;
                }
            });
        }

        public List<DependencyNode> listNonConflicts() {
            List<DependencyNode> all = Lists.newLinkedList();
            for (String name : byName.keySet()) {
                Collection<DependencyNode> libs = byName.get(name);
                if (libs.size() == 1) {
                    all.addAll(libs);
                }
            }
            // sort by name (ascending)
            Collections.sort(all, new Comparator<DependencyNode>() {
                public int compare(DependencyNode o1, DependencyNode o2) {
                    int c = o1.org.compareTo(o2.org);
                    if (c == 0) {
                        c = o1.name.compareTo(o2.name);
                    }
                    return c;
                }
            });
            return all;
        }
    }

    public static final class DependencyNode {
        private final String org;
        private final String name;
        private final String path;
        private final String rev;
        private final Set<DependencyNode> dependencies = Sets.newHashSet();
        private final Set<DependencyNode> dependents = Sets.newHashSet();

        DependencyNode(String org, String name, String path, String rev) {
            this.org = org;
            this.name = name;
            this.path = path;
            this.rev = rev;
        }

        DependencyNode(IvyDependency dep, ReleaseEnvironment env) {
            this(dep.getOrg(), dep.getName(), dep.getPath(), "".equals(dep.getRev()) ? dep.getLatestRev() : dep.getRev());
        }

        public String getOrg() {
            return org;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public String getRev() {
            return rev;
        }

        public void addDependency(DependencyNode dep) {
            dependencies.add(dep);
            dep.dependents.add(this);
        }

        public Set<DependencyNode> getDependencies() {
            return dependencies;
        }

        public Set<DependencyNode> getDependents() {
            return dependents;
        }

        @Override
        public String toString() {
            return org + "/" + name + ";" + rev;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DependencyNode that = (DependencyNode) o;

            if (org != null ? !org.equals(that.org) : that.org != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (rev != null ? !rev.equals(that.rev) : that.rev != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (rev != null ? rev.hashCode() : 0);
            return result;
        }
    }



    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        ReleaseEnvironment env = new ReleaseEnvironment();
        VCSClient cli = new SubversionClient(env);
        IvyLoader loader = new IvyLoader(cli, env);
//        List<IvyDependency> deps = loader.loadDependencies();
//        System.out.println(deps);
//        IvyDependency dep = deps.get(Integer.parseInt(args[2]));
//        System.out.println("upgrade from " + dep);
//        IvyDependency up = new IvyDependency(dep.getOrg(), dep.getName(), dep.getPath(), dep.getRev(), true, args[3]);
//        System.out.println(loader.considerUpgrade(env, deps, dep, up));
/*        DepGraph g = new DepGraph(cli, env, args[0], !args[1].contains("2010"), args[1]);
        g.build(System.out);
        System.out.println("All Conflicts:");
        for (DependencyNode node : g.listAllConflicts()) {
            System.out.println("* " + node + ", from: " + node.getDependents());
        }
        System.out.println(args[2] + ": " + g.findByName(args[2]));
        System.out.println("Potential Conflicts for " + args[2] + " " + args[3] + ":");
        for (DependencyNode dependencyNode : g.findConflicts(args[2], args[3])) {
            System.out.println("* " + dependencyNode + ", from: " + dependencyNode.getDependents());
        }
    */
    }
}
