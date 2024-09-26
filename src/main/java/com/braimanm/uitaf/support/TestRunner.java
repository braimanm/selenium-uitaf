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
import com.braimanm.uitaf.utils.CommandLine;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.util.PropertiesUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.TestNG;

import java.io.*;
import java.nio.file.Files;
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

	public void unpackAllureConfig() throws IOException {
		String homeFolder = FileUtils.getTempDirectoryPath() + "allurehome";
		File targetDir = new File(homeFolder);
		FileUtils.deleteDirectory(targetDir);
		//FileUtils.forceMkdir(targetDir);
		System.setProperty("APP_HOME", homeFolder);

		try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("allurehome/allureconfig.zip");
			 InputStream bis = new BufferedInputStream(is);
			 ArchiveInputStream<ZipArchiveEntry> archIs = new ZipArchiveInputStream(bis,null, true, true)) {

			ArchiveEntry entry;

			while ((entry = archIs.getNextEntry()) != null) {
				if (!archIs.canReadEntryData(entry)) {
					// log something?
					continue;
				}
				String name = targetDir.getAbsolutePath() + "/" + entry;
				File f = new File(name);
				if (entry.isDirectory()) {
					if (!f.isDirectory() && !f.mkdirs()) {
						throw new IOException("failed to create directory " + f);
					}
				} else {
					File parent = f.getParentFile();
					if (!parent.isDirectory() && !parent.mkdirs()) {
						throw new IOException("failed to create directory " + parent);
					}
					try (OutputStream o = Files.newOutputStream(f.toPath())) {
						IOUtils.copy(archIs, o);
					}
				}
			}
		}
	}

	public void generateReport() throws IOException, InterruptedException {
		unpackAllureConfig();
		String[] arguments = {"generate",resultsFolder,"-o",reportFolder,"--name","UITAF Report"};
		CommandLine.main(arguments);
		//brandingMod();
	}

	public void openReport() throws InterruptedException {
		String[] arguments = {"open",reportFolder,"-h","localhost","-p", "" + TestContext.getTestProperties().getReportPort()};
		CommandLine.main(arguments);
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

}