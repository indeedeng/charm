package com.indeed.charm.ivy;

import com.indeed.charm.ReleaseEnvironment;

import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 */
public class IvyUtils {
    private final ReleaseEnvironment env;

    public IvyUtils(ReleaseEnvironment env) {
        this.env = env;
    }

    public boolean upgradeDependency(Reader ivyFileReader, Writer newFileWriter, String org, String module, String oldRev, String newRev) throws IOException {
        boolean changed = false;
        Pattern oldRevPattern = Pattern.compile("org=\"" + org + "\".*name=\"" + module + "\".*rev=\"" + oldRev + "\"");
        BufferedReader rdr = new BufferedReader(ivyFileReader);
        PrintWriter writer = new PrintWriter(newFileWriter);
        try {
            String line;
            while ((line = rdr.readLine()) != null) {
                final Matcher m = oldRevPattern.matcher(line);
                if (m.find()) {
                    line = line.replace("rev=\"" + oldRev + "\"", "rev=\"" + newRev + "\"");
                    changed = true;
                }
                writer.println(line);
            }
        } finally {
            writer.flush();
            writer.close();
            rdr.close();
        }
        return changed;
    }
}
