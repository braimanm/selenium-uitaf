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

package com.braimanm.uitaf.support;

import com.braimanm.uitaf.testng.TestParameterValidator;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.util.PropertiesUtils;

import org.apache.commons.io.FileUtils;
import org.testng.TestNG;

import java.io.*;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("unused")
public class TestRunner {
	private final String resultsFolder;
	private final String reportFolder;

	public TestRunner(){
		TestProperties props = TestContext.getTestProperties();
		System.setProperty("allure.results.directory", props.getResultsFolder().getAbsolutePath());
		System.setProperty("allure.testng.parameters.enabled", "false");
		Properties properties = PropertiesUtils.loadAllureProperties();
		resultsFolder = properties.getProperty("allure.results.directory", "allure-results");
		reportFolder = props.getReportFolder().getAbsolutePath();
		props.setReportUrlPatterns();
	}

	public int runTests(List<String> suites) throws IOException{
		if (suites.isEmpty()) {
			throw new RuntimeException("Suite file names where not provided! \nPlease provide suite file names Ex: -Dtest.suites=<SUITE FILE PATH>");
		}
		for (String suite:suites){
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(suite);
			File file = new File(suite);
			if (is != null) {
				FileUtils.copyInputStreamToFile(is, file);
			} else if (!file.exists()) {
				throw new RuntimeException("Suite file '" + suite + "' does not exists on the file system!");
			}
		}
		TestNG testNg = new TestNG(false);
		testNg.addListener(new TestParameterValidator());
		testNg.setTestSuites(suites);
		testNg.setSuiteThreadPoolSize(1);
		testNg.run();
		saveEnvironment();
		return testNg.getStatus();
	}


	private void saveEnvironment() throws IOException {
		TestProperties props = TestContext.getTestProperties();
		List<Parameter> params = props.getAsParameters();
		File file = new File(resultsFolder + "/environment.properties");
		StringBuilder sb = new StringBuilder();
		params.forEach(parameter -> sb.append(parameter.getName()).append("=").append(parameter.getValue()).append("\n"));
		FileUtils.write(file, sb.toString());
	}

	public void deleteResultsFolder() throws IOException {
		File resFolder = new File(resultsFolder);
		if (resFolder.exists()) {
			FileUtils.forceDelete(resFolder);
		}
	}

	public void deleteReportFolder() throws IOException {
		File resFolder = new File(reportFolder);
		if (resFolder.exists()) {
			FileUtils.forceDelete(resFolder);
		}
	}

	public void openAllureReport() {
		try {
			// Check if Allure CLI is installed
			Process checkAllure = new ProcessBuilder("allure", "--version")
					.redirectErrorStream(true)
					.start();

			if (checkAllure.waitFor() != 0) {
				System.err.println("Allure CLI not found. Please install it from:");
				System.err.println("https://allurereport.org/docs/install/");
				return;
			}

			// Serve the Allure report (this also opens it in the browser)
			Process serveReport = new ProcessBuilder("allure", "serve", resultsFolder)
					.inheritIO()
					.start();

			serveReport.waitFor();

		} catch (IOException | InterruptedException e) {
			System.err.println("Failed to run Allure. Make sure Allure CLI is in your PATH.");
			e.printStackTrace();
		}
	}

}