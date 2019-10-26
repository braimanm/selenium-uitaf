/*
Copyright 2010-2019 Michael Braiman braimanm@gmail.com
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package ui.auto.core;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.auto.core.support.TestContext;
import ui.auto.core.support.TestProperties;
import ui.auto.core.support.TestRunner;

import java.awt.*;
import java.net.URI;
import java.util.Date;

public class RunTests {
    private final static Logger logger = LoggerFactory.getLogger(RunTests.class);

    private static boolean isRunningOnJenkins() {
        return System.getenv().containsKey("JENKINS_HOME");
    }

    public static void main(String[] args) throws Exception {
        int status;
        String msgTemplate = "\u001B[34m%s\u001B[0m";

        Date startTime = new Date();
        logger.info(String.format(msgTemplate, "Started At:  " + startTime));

        logger.info(String.format(msgTemplate, "...STARTING SUITE EXECUTION..."));
        TestProperties props = TestContext.getTestProperties();
        TestRunner runner = new TestRunner();
        logger.info(String.format(msgTemplate, "...CLEANING RESULT FOLDER..."));
        runner.deleteResultsFolder();
        logger.info(String.format(msgTemplate, "...CLEANING REPORT FOLDER..."));
        runner.deleteReportFolder();
        status = runner.runTests(props.getSuites());
        logger.info(String.format(msgTemplate, "...SUITE EXECUTION IS FINISHED, GENERATING ALLURE REPORT...\n"));
        runner.generateReport();
        logger.info(String.format(msgTemplate, "...ALLURE REPORT IS GENERATED..."));
        Date endTime = new Date();
        logger.info(String.format(msgTemplate, "Completed At:  " + endTime));
        String duration = DurationFormatUtils.formatPeriod(startTime.getTime(), endTime.getTime(), "HH:mm:ss");
        logger.info(String.format(msgTemplate, "Duration:  " + duration));

        if (props.isShowReport() && !isRunningOnJenkins()) {
            logger.info(String.format(msgTemplate, "...OPENING ALLURE REPORT..."));
            openReport();
        }

        System.exit(status);
    }

    private static void openReport() throws Exception {
        Server server = setUpServer();
        server.start();
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI("http://localhost:" +
                    TestContext.getTestProperties().getReportPort()));
        }
        server.join();
    }

    private static Server setUpServer() {
        Server server = new Server(TestContext.getTestProperties().getReportPort());
        ResourceHandler handler = new ResourceHandler();
        handler.setDirectoriesListed(true);
        handler.setWelcomeFiles(new String[]{"index.html"});
        handler.setResourceBase(TestContext.getTestProperties().getReportFolder().getAbsolutePath());
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{handler, new DefaultHandler()});
        server.setStopAtShutdown(true);
        server.setHandler(handlers);
        server.setStopAtShutdown(true);
        return server;
    }

}