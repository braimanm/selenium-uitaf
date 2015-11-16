package ui.auto.core.support;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.Platform;

import ru.yandex.qatools.commons.model.Parameter;
import ru.yandex.qatools.properties.PropertyLoader;
import ru.yandex.qatools.properties.annotations.Property;
import ru.yandex.qatools.properties.annotations.Resource;
import ru.yandex.qatools.properties.annotations.Use;

@Resource.Classpath("test.properties")
public class TestProperties {
	static TestProperties testProperties;
	
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
	
	@Property("timeout.page")
	private int page_timeout; //In milliseconds
	
	@Property("timeout.element")
	private int element_timeout; //In seconds

	@Property("test.suites")
	private String suites;
	
	@Property("report.folder")
	protected File reportFolder = new File("target/allure-report");
	
	@Property("report.show")
	private boolean showReport;
	
	public static TestProperties getInstance() {
		if (testProperties == null) {
			testProperties = new TestProperties();
			PropertyLoader.populate(testProperties);
		}
		return testProperties;
	}
	
	private TestProperties() {
		populateEnvProp();
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
	
	
}
