/*
Copyright 2010-2024 Michael Braiman braimanm@gmail.com
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

package com.braimanm.uitaf;

import com.braimanm.uitaf.support.TestContext;
import com.braimanm.uitaf.support.TestProperties;
import com.braimanm.uitaf.support.TestRunner;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class RunTests {
    private final static Logger logger = LoggerFactory.getLogger(RunTests.class);

    private static boolean isNotRunningOnJenkins() {
        return !System.getenv().containsKey("JENKINS_HOME");
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
        status = runner.runTests(props.getSuites());
        logger.info(String.format(msgTemplate, "...SUITE EXECUTION IS FINISHED\n"));

        if (props.isShowReport() && isNotRunningOnJenkins()) {
            logger.info(String.format(msgTemplate, "...GENERATING ALLURE REPORT...\n"));
            runner.generateReport();
            logger.info(String.format(msgTemplate, "...ALLURE REPORT IS GENERATED..."));
        }

        Date endTime = new Date();
        logger.info(String.format(msgTemplate, "Completed At:  " + endTime));
        String duration = DurationFormatUtils.formatPeriod(startTime.getTime(), endTime.getTime(), "HH:mm:ss");
        logger.info(String.format(msgTemplate, "Duration:  " + duration));

        if (props.isShowReport() && isNotRunningOnJenkins()) {
            logger.info(String.format(msgTemplate, "...OPENING ALLURE REPORT..."));
            runner.openReport();
        }

        System.exit(status);
    }

}