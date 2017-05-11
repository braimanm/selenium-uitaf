package ui.auto.core.support;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;
import ru.yandex.qatools.commons.model.Parameter;
import ru.yandex.qatools.properties.PropertyLoader;
import ru.yandex.qatools.properties.annotations.Property;
import ru.yandex.qatools.properties.annotations.Resource;
import ru.yandex.qatools.properties.annotations.Use;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Resource.Classpath("test.properties")
public class TestProperties {
	static TestProperties testProperties;
	@Property("report.folder")
	protected File reportFolder = new File("target/allure-report");
	@Property("app.url")
	private String autURL;
	@Property("app.user")
	private String appUser;
	@Property("app.password")
	private String appPassword;
	@Property("webdriver.remote.url")
	private String remoteURL;
	@Use(BrowserPlatformPropertyConverter.class)
	@Property("webdriver.browser.platform")
	private Platform platform;
	@Property("webdriver.browser.version")
	private String version;
	@Use(BrowserTypePropertyConverter.class)
	@Property("webdriver.browser.type")
	private WebDriverTypeEnum browserType = WebDriverTypeEnum.FIREFOX;
	@Property("webdriver.extra.capabilities")
	private String extraCapabilities;
	@Property("webdriver.http.proxy")
	private String httpProxy;
	@Property("webdriver.https.proxy")
	private String httpsProxy;
	@Property(("webdriver.screen.size"))
	private String screenSize;
	@Property("webdriver.accept.ssl.certs")
	private boolean acceptSSLCerts;
	@Property("timeout.page")
	private int page_timeout; //In milliseconds
	@Property("timeout.element")
	private int element_timeout; //In seconds
	@Property("test.suites")
	private String suites;
	@Property("report.port")
	private int reportPort = 8090;
	@Property("report.show")
	private boolean showReport;

	@Property("user.agent")
	private String userAgent;

	@Property("test.parallel.threads")
	private Integer threadCount;

	@Property("test.default.retry")
	private int testDefaultRetry = 2;

	@Property("firefox.bin")
	private String firefoxBin;

	@Property("firefox.no.marionette")
	private boolean useNoMarionette;

	private TestProperties() {
		populateEnvProp();
	}
	
	public static TestProperties getInstance() {
		if (testProperties == null) {
			testProperties = new TestProperties();
			PropertyLoader.populate(testProperties);
		}
		return testProperties;
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

	public String getAutURL() {
		return autURL;
	}

	public String getAppUser() {
		return appUser;
	}

	public String getAppPassword() {
		return appPassword;
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
				suitesList.add(s.trim().replace("\"","").replace("\'",""));
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

	public int getReportPort() {
		return reportPort;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		for (Field field : this.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Property.class)) {
				String prop = field.getAnnotation(Property.class).value();
				String value = null;
				try {
					value = field.get(this).toString();
				} catch (Exception e) {
					//do nothing
				}
				str.append(prop + " = " + value + "\n");
			}
		}
		return str.toString();
	}

	public List<Parameter> getAsParameters(){
		List<Parameter> params=new ArrayList<>();
		for (Field field: this.getClass().getDeclaredFields()){
			if (field.isAnnotationPresent(Property.class)){
				String property= field.getAnnotation(Property.class).value();
				String value;
				try {
					value=field.get(this).toString();
				} catch (Exception e ) {
					value="";
				}
				params.add(new Parameter().withKey(property).withName(property).withValue(value));
			}
		}
		return params;
	}

	public Capabilities getExtraCapabilities() {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		if (extraCapabilities != null) {
			String params[] = extraCapabilities.split(",");
			for (String param : params) {
				String values[] = param.split("=", 2);
				capabilities.setCapability(values[0].trim(), values[1].trim());
			}
		}
		return capabilities;
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

	public String getFirefoxBin() {
		return firefoxBin;
	}

	public boolean noMarionette() {
		return useNoMarionette;
	}

	public int getTestDefaultRetry() {
		return testDefaultRetry;
	}

	public String getScreenSize() {
		return screenSize;
	}
	
}
