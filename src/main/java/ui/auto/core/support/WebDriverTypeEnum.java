package ui.auto.core.support;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

public enum WebDriverTypeEnum {
	CHROME ("chrome",ChromeDriver.class),
	EDGE ("MicrosoftEdge",EdgeDriver.class),
	FIREFOX ("firefox",FirefoxDriver.class),
	IE ("internet explorer",InternetExplorerDriver.class),
	OPERA_BLINK ("operablink",OperaDriver.class),
	SAFARI ("safari",SafariDriver.class),
	HTMLUNIT ("htmlunit",HtmlUnitDriver.class),
	_OTHER ("",RemoteWebDriver.class);
	
	String driverName;
	Class<? extends WebDriver> driverClass;
	private WebDriverTypeEnum(String driverName, Class<? extends WebDriver> driverClass) {
		this.driverClass = driverClass;
		this.driverName = driverName;
	}
	
	public String getDriverName() {
		return driverName;
	}
	
	public WebDriver getNewWebDriver() {
		TestProperties prop = TestProperties.getInstance();
		if (prop.getRemoteURL()!=null) {
			try {
				String browser = getDriverName();
				String version = prop.getBrowserVersion();
				Platform platform = prop.getBrowserPlatform();
				if (platform==null) {
					platform = Platform.ANY;
				}
				DesiredCapabilities capabilities = new DesiredCapabilities(browser, version, platform);
				return new RemoteWebDriver(new URL(prop.getRemoteURL()),capabilities);
			} catch (MalformedURLException e) {
				throw new RuntimeException("Malformed URL!");
			}
		}
		if(this.equals(_OTHER)){
			return new RemoteWebDriver(new DesiredCapabilities());
		}
		try {
			return driverClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Can't instantiate WebDriver type " + driverClass.getName());
		}
	}
	
}
