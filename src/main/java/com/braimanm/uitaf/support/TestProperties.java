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
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;
import ru.qatools.properties.Property;
import ru.qatools.properties.PropertyLoader;
import ru.qatools.properties.Resource;
import ru.qatools.properties.Use;

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
	private File resultsFolder = new File("target/results");
	@Hide
	@Property("report.folder")
	private File reportFolder = new File("target/report");
	@Property("report.port")
	private int reportPort = 8090;
	@Property("report.show")
	private boolean showReport;
	@Property("webdriver.remote.url")
	private String remoteURL;
	@Hide
	@Use(BrowserPlatformPropertyConverter.class)
	@Property("webdriver.browser.platform")
	private Platform platform;
	@Hide
	@Property("webdriver.browser.version")
	private String version;
	@Use(DriverProviderConverter.class)
	@Property("driver.provider")
	private DriverProvider driverProvider = new DefaultDriverProvider();
	@Use(BrowserTypePropertyConverter.class)
	@Property("webdriver.browser.type")
	private WebDriverTypeEnum browserType = WebDriverTypeEnum.FIREFOX;
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
	private int testDefaultRetry = 2;
	@Property("webdriver.install")
	private boolean installDrivers = false;
	@Hide
	@Property("report.tms.url")
	private String tmsUrlPattern;
	@Hide
	@Property("report.issue.url")
	private String issueUrlPattern;

	TestProperties() {
		populateEnvProp();
		PropertyLoader.newInstance().populate(this);
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

	private void populateEnvProp(){
		for (Field field : this.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Property.class)) {
				String prop = field.getAnnotation(Property.class).value();
				String value = getEnvValue(prop.replace(".", "_"));
				if (value!=null) {
					System.setProperty(prop, value);
				}
			}
		}
	}

	private String getEnvValue(String prop) {
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
	public Platform getBrowserPlatform() {
		return platform;
	}
	public String getBrowserVersion() {
		return version;
	}
	public WebDriverTypeEnum getBrowserType() {
		return browserType;
	}
	public void setBrowserType(WebDriverTypeEnum browserType) {
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

	public List<Parameter> getAsParameters(){
		List<Parameter> params=new ArrayList<>();
		if (testEnvironment != null) {
			params.add(new Parameter().setName("test.env.name").setValue(testEnvironment.getEnvironmentName()));
			params.add(new Parameter().setName("test.env.url").setValue(testEnvironment.getUrl()));
		}
		for (Field field : this.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Property.class) && ! field.isAnnotationPresent(Hide.class)) {
				String property= field.getAnnotation(Property.class).value();
				String value;
				try {
					value=field.get(this).toString();
				} catch (Exception e ) {
					value="";
				}
				params.add(new Parameter().setName(property).setValue(value));
			}
		}
		return params;
	}

	public Capabilities getExtraCapabilities() {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		if (extraCapabilities != null) {
			String[] params = extraCapabilities.split(",");
			for (String param : params) {
				String[] values = param.split("=", 2);
				String key = values[0].trim();
				String value = values[1].trim();
				if (key.contains("securityToken") && System.getenv().containsKey("SEC_TOKEN")) {
					value = System.getenv("SEC_TOKEN");
				}
				capabilities.setCapability(key, value);
			}
		}
		return capabilities;
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
	public DriverProvider getDriverProvider() {
		return driverProvider;
	}
	public boolean getHeadless() {
		return headless;
	}

}