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

import io.qameta.allure.model.Parameter;
import ru.qatools.properties.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "unused"})
@Resource.Classpath("test.properties")
public class TestProperties {
	private String reportVersion = "1.0.1";
	@Hide
	@Property("test.env")
	@Use(EnvironmentPropertyConverter.class)
	private EnvironmentsSetup.Environment testEnvironment;
	@Hide
	@Property("report.results")
	@DefaultValue("target/results")
	private File resultsFolder;
	@Hide
	@Property("report.folder")
	@DefaultValue("target/report")
	private File reportFolder;
	@Property("report.port")
	@DefaultValue("8090")
	private int reportPort;
	@Property("report.show")
	private boolean showReport;
	@Property("webdriver.remote.url")
	private String remoteURL;
	@Hide
	@Property("webdriver.browser.platform")
	private String platform;
	@Hide
	@Property("webdriver.browser.version")
	private String version;
	@Property("webdriver.browser.type")
	@DefaultValue("CHROME")
	private String browserType;
	@Property("webdriver.headless")
	private boolean headless;
	@Hide
	@Property("webdriver.extra.capabilities")
	private String extraCapabilities;
	@Hide
	@Property("webdriver.http.proxy")
	private String httpProxy;
	@Hide
	@Property("webdriver.https.proxy")
	private String httpsProxy;
	@Property("webdriver.screen.size")
	private String screenSize;
	@Hide
	@Property("webdriver.accept.ssl.certs")
	private boolean acceptSSLCerts;
	@Property("timeout.page")
	private int page_timeout; //In milliseconds
	@Property("timeout.element")
	private int element_timeout; //In seconds
	@Property("test.suites")
	private String suites;
	@Hide
	@Property("user.agent")
	private String userAgent;
	@Property("test.parallel.threads")
	private Integer threadCount;
	@Property("test.default.retry")
	@DefaultValue("2")
	private int testDefaultRetry;
	@Property("webdriver.install")
	@DefaultValue("false")
	private boolean installDrivers;
	@Hide
	@Property("report.tms.url")
	private String tmsUrlPattern;
	@Hide
	@Property("report.issue.url")
	private String issueUrlPattern;

	public TestProperties() {
		populateEnvProp();
		PropertyLoader loader = PropertyLoader.newInstance();
		Class<?> current = this.getClass();
		while (current != null && current != Object.class) {
			try {
				if (this.getClass().equals(current)) {
					loader.populate(this);
				} else {
					loader.populate(current.getDeclaredConstructor().newInstance());
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			current = current.getSuperclass();
		}
	}

	public void setReportUrlPatterns() {
		if (issueUrlPattern != null) {
			System.setProperty("allure.issues.tracker.pattern", issueUrlPattern);
		}
		if (tmsUrlPattern != null) {
			System.setProperty("allure.tests.management.pattern", tmsUrlPattern);
		}
	}

	public EnvironmentsSetup.Environment getTestEnvironment() {
		return testEnvironment;
	}

	protected void populateEnvProp() {
		Class<?> current = this.getClass();
		while (current != null && current != Object.class) {
			for (Field field : current.getDeclaredFields()) {
				if (field.isAnnotationPresent(Property.class)) {
					String prop = field.getAnnotation(Property.class).value();
					String value = getEnvValue(prop.replace(".", "_"));
					if (value != null) {
						System.setProperty(prop, value);
					}
				}
			}
			current = current.getSuperclass();
		}
	}

	protected String getEnvValue(String prop) {
		for (String key : System.getenv().keySet()) {
			if (prop.equalsIgnoreCase(key)) {
				return System.getenv(key);
			}
		}
		return null;
	}

	public String getRemoteURL() {
		return remoteURL;
	}
	public String getBrowserPlatform() {
		return platform;
	}
	public String getBrowserVersion() {
		return version;
	}
	public String getBrowserType() {
		return browserType;
	}
	public void setBrowserType(String browserType) {
		this.browserType=browserType;
	}
	public int getPageTimeout() {
		return page_timeout;
	}
	public int getElementTimeout() {
		return element_timeout;
	}

	public List<String> getSuites(){
		List<String> suitesList = new ArrayList<>();
		if (this.suites!=null){
			String[] suites = this.suites.split(",");
			for (String s : suites){
				suitesList.add(s.trim().replace("\"","").replace("'",""));
			}
		}
		return suitesList;
	}

	public boolean isShowReport() {
		return showReport;
	}
	public File getReportFolder() {
		return reportFolder;
	}
	public File getResultsFolder() {
		return resultsFolder;
	}
	public int getReportPort() {
		return reportPort;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (Field field : this.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Property.class)) {
				String prop = field.getAnnotation(Property.class).value();
				String value = null;
				try {
					value = field.get(this).toString();
				} catch (Exception e) {
					//do nothing
				}
				str.append(prop).append(" = ").append(value).append("\n");
			}
		}
		return str.toString();
	}

	public List<Parameter> getAsParameters() {
		List<Parameter> params = new ArrayList<>();

		if (testEnvironment != null) {
			params.add(new Parameter().setName("test.env.name").setValue(testEnvironment.getEnvironmentName()));
			params.add(new Parameter().setName("test.env.url").setValue(testEnvironment.getUrl()));
		}

		Class<?> current = this.getClass();
		while (current != null && current != Object.class) {
			for (Field field : current.getDeclaredFields()) {
				field.setAccessible(true);
				if (field.isAnnotationPresent(Property.class) && !field.isAnnotationPresent(Hide.class)) {
					String property = field.getAnnotation(Property.class).value();
					try {
						Object raw = field.get(this);
						String value = (raw != null) ? raw.toString() : "";
						params.add(new Parameter().setName(property).setValue(value));
					} catch (IllegalAccessException e) {
						// Handle or ignore field access errors
						params.add(new Parameter().setName(property).setValue(""));
					}
				}
			}
			current = current.getSuperclass();
		}

		return params;
	}

	public String getExtraCapabilities() {
		return extraCapabilities;
	}

	public void setExtraCapabilities(String capabilities) {
		this.extraCapabilities = capabilities;
	}

	public void replaceExtraCapabilities(String target, String replacement) {
		extraCapabilities = extraCapabilities.replace(target, replacement);
	}

	public String getHttpProxy() {
		return httpProxy;
	}
	public String getHttpsProxy() {
		return httpsProxy;
	}
	public boolean getAcceptSSLCerts() {
		return acceptSSLCerts;
	}
	public String getUserAgent() {
		return userAgent;
	}
	public Integer getThreadCount() {
		return threadCount;
	}
	public int getTestDefaultRetry() {
		return testDefaultRetry;
	}
	public String getScreenSize() {
		return screenSize;
	}
	public String getReportVersion() {
		return reportVersion;
	}
	public boolean installDrivers() {
		return installDrivers;
	}
	public boolean getHeadless() {
		return headless;
	}

}