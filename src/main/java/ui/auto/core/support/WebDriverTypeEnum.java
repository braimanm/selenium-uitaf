package ui.auto.core.support;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;

public enum WebDriverTypeEnum {
	CHROME ("chrome",ChromeDriver.class),
	EDGE ("MicrosoftEdge",EdgeDriver.class),
	FIREFOX ("firefox",FirefoxDriver.class),
	IE ("internet explorer",InternetExplorerDriver.class),
	OPERA_BLINK ("operablink",OperaDriver.class),
    SAFARI("safari", SafariDriver.class);

    String driverName;
	Class<? extends WebDriver> driverClass;

    WebDriverTypeEnum(String driverName, Class<? extends WebDriver> driverClass) {
        this.driverClass = driverClass;
		this.driverName = driverName;
	}
	
	public String getDriverName() {
		return driverName;
	}
	
	public WebDriver getNewWebDriver() {
        TestProperties prop = TestProperties.getInstance();
        DesiredCapabilities capabilities = new DesiredCapabilities();
        Proxy proxy = setupWebDriverProxy();
        if (proxy != null) {
            capabilities.setCapability(CapabilityType.PROXY, proxy);
        }
        if (prop.getAcceptSSLCerts()) {
            capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        }

        if (prop.getRemoteURL() != null) {
            capabilities.merge(getCapabilities());
            try {
                return new RemoteWebDriver(new URL(prop.getRemoteURL()), capabilities);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Malformed URL!", e);
            }
        }

        switch (this) {
            case FIREFOX:
                FirefoxProfile profile = new FirefoxProfile();
                profile.setPreference("focusmanager.testmode", true);

                if (prop.getAcceptSSLCerts()) {
                    profile.setAcceptUntrustedCertificates(true);
                    profile.setAssumeUntrustedCertificateIssuer(false);
                }

                if (prop.getUserAgent() != null) {
                    profile.setPreference("general.useragent.override", prop.getUserAgent());
                }

                capabilities.setCapability(FirefoxDriver.PROFILE, profile);

                if (prop.getFirefoxBin() != null) {
                    System.setProperty("webdriver.firefox.bin", prop.getFirefoxBin());
                }

                if (prop.noMarionette()) {
                    System.setProperty("webdriver.firefox.marionette", "false");
                }

                return new FirefoxDriver(capabilities);

            case CHROME:
                if (prop.getUserAgent() != null) {
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("user-agent=" + prop.getUserAgent());
                    capabilities.setCapability(ChromeOptions.CAPABILITY, options);
                }
                return new ChromeDriver(capabilities);

            case SAFARI:
                capabilities.merge(getCapabilities());
                SafariOptions options = SafariOptions.fromCapabilities(capabilities);
                options.setUseCleanSession(true);
                //options.setUseTechnologyPreview(true);
                capabilities.setCapability("safari.options", options);
                return new SafariDriver(capabilities);
            default:
                try {
                    Constructor<? extends WebDriver> constructor = driverClass.getConstructor(Capabilities.class);
                    return constructor.newInstance(capabilities);
                } catch (Exception e) {
                    throw new RuntimeException("Can't instantiate WebDriver type " + driverClass.getName(), e);
                }
        }

    }

    protected Proxy setupWebDriverProxy() {
        TestProperties prop = TestProperties.getInstance();
        Proxy proxy = null;
        if (prop.getHttpProxy() != null) {
            proxy = new Proxy();
            proxy.setHttpProxy(prop.getHttpProxy());
        }
        if (prop.getHttpsProxy() != null) {
            if (proxy == null) {
                proxy = new Proxy();
            }
            proxy.setSslProxy(prop.getHttpsProxy());
        }
        return proxy;
    }

    protected DesiredCapabilities getCapabilities() {
        TestProperties prop = TestProperties.getInstance();
        Platform platform = prop.getBrowserPlatform();
        if (platform == null) {
            platform = Platform.ANY;
        }
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setBrowserName(getDriverName());
        capabilities.setVersion(prop.getBrowserVersion());
        capabilities.setPlatform(platform);
        capabilities.merge(prop.getExtraCapabilities());
        return capabilities;
    }
	
}
