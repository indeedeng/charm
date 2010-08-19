/*
 * Copyright (C) 2010 Indeed Inc.
 *
 * This file is part of CHARM.
 *
 * CHARM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CHARM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CHARM.  If not, see <http://www.gnu.org/licenses/>.
 */

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
