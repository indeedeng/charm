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

package com.indeed.charm;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionBindingEvent;

import com.indeed.charm.svn.SubversionClient;
import com.indeed.charm.actions.BackgroundJobManager;
import com.google.common.collect.MapMaker;

/**
 */
public class ContextListener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {

    private static Logger log = Logger.getLogger(ContextListener.class);

    // Public constructor is required by servlet spec
    public ContextListener() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
        final ServletContext ctx = sce.getServletContext();
        String log4jConfig = ctx.getInitParameter("log4jConfigLocation");
        if (log4jConfig == null) {
            log4jConfig = System.getProperty("log4jConfigLocation");
        }
        if (log4jConfig != null) {
            DOMConfigurator.configure(log4jConfig);
            log.info("Configured: " + log4jConfig);
        } else {
            // try properties file
            log4jConfig = ctx.getInitParameter("log4j.configuration");
            if (log4jConfig != null) {
                PropertyConfigurator.configure(log4jConfig);
            } else {
                BasicConfigurator.configure();
                Logger.getRootLogger().setLevel(Level.WARN);
                Logger.getLogger("com.indeed.charm").setLevel(Level.INFO);
            }
        }

        try {
            final ReleaseEnvironment env = new ReleaseEnvironment(sce.getServletContext());
            ctx.setAttribute(ReleaseEnvironment.class.getSimpleName(), env);
            // TODO: select VCS client based on charm.properties
            final VCSClient svnClient = new SubversionClient(env);
            ctx.setAttribute(VCSClient.class.getSimpleName(), svnClient);
            final ProjectsLoader projectsLoader = new ProjectsLoader(env, svnClient);
            ctx.setAttribute(ProjectsLoader.class.getSimpleName(), projectsLoader);
            final BackgroundJobManager backgroundJobManager = new BackgroundJobManager();
            ctx.setAttribute(BackgroundJobManager.class.getSimpleName(), backgroundJobManager);
            ctx.setAttribute("DepGraphCache", new MapMaker().softValues().makeMap());

            env.scheduleCleanupTask();

        } catch (VCSException e) {
            log.error("Unable to initialize subversion", e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        /* This method is invoked when the Servlet Context 
           (the Web application) is undeployed or 
           Application Server shuts down.
        */
    }

    // -------------------------------------------------------
    // HttpSessionListener implementation
    // -------------------------------------------------------
    public void sessionCreated(HttpSessionEvent se) {
        /* Session is created. */
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        /* Session is destroyed. */
    }

    // -------------------------------------------------------
    // HttpSessionAttributeListener implementation
    // -------------------------------------------------------

    public void attributeAdded(HttpSessionBindingEvent sbe) {
        /* This method is called when an attribute 
           is added to a session.
        */
    }

    public void attributeRemoved(HttpSessionBindingEvent sbe) {
        /* This method is called when an attribute
           is removed from a session.
        */
    }

    public void attributeReplaced(HttpSessionBindingEvent sbe) {
        /* This method is invoked when an attibute
           is replaced in a session.
        */
    }
}
