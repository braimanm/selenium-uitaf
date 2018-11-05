package ui.auto.core.support;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public enum WebDriverTypeEnum {
    CHROME(BrowserType.CHROME),
    EDGE(BrowserType.EDGE),
    FIREFOX(BrowserType.FIREFOX),
    IE(BrowserType.IE),
    OPERA_BLINK(BrowserType.OPERA_BLINK),
    SAFARI(BrowserType.SAFARI);

    String driverName;

    WebDriverTypeEnum(String driverName) {
        this.driverName = driverName;
    }

    private Proxy setupWebDriverProxy() {
        TestProperties prop = TestContext.getTestProperties();
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

    private DesiredCapabilities getCapabilities() {
        TestProperties prop = TestContext.getTestProperties();
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

    private WebDriver getRemoteWebDriver(String url, MutableCapabilities capabilities) {
        try {
            return new RemoteWebDriver(new URL(url), capabilities);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL!", e);
        }
    }

    public String getDriverName() {
        return driverName;
    }

    public WebDriver getNewWebDriver(Consumer<MutableCapabilities> capabilities) {
        TestProperties prop = TestContext.getTestProperties();
        DesiredCapabilities desiredCapabilities = getCapabilities();
        Proxy proxy = setupWebDriverProxy();
        if (proxy != null) {
            desiredCapabilities.setCapability(CapabilityType.PROXY, proxy);
        }
        if (prop.getAcceptSSLCerts()) {
            desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            desiredCapabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
        }
        switch (this) {
            case FIREFOX:
                FirefoxProfile profile = new FirefoxProfile();
                profile.setPreference("focusmanager.testmode", true);
                FirefoxOptions firefoxOptions = new FirefoxOptions(desiredCapabilities);
                firefoxOptions.setProfile(profile);
                if (capabilities != null) {
                    capabilities.accept(firefoxOptions);
                }
                if (prop.getRemoteURL() != null) {
                    return getRemoteWebDriver(prop.getRemoteURL(), firefoxOptions);
                } else {
                    return new FirefoxDriver(firefoxOptions);
                }

            case CHROME:
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.merge(desiredCapabilities);
                if (capabilities != null) {
                    capabilities.accept(chromeOptions);
                }
                if (prop.getRemoteURL() != null) {
                    return getRemoteWebDriver(prop.getRemoteURL(), chromeOptions);
                } else {
                    return new ChromeDriver(chromeOptions);
                }

            case SAFARI:
                SafariOptions safariOptions = SafariOptions.fromCapabilities(desiredCapabilities);
                if (capabilities != null) {
                    capabilities.accept(safariOptions);
                }
                if (prop.getRemoteURL() != null) {
                    return getRemoteWebDriver(prop.getRemoteURL(), safariOptions);
                } else {
                    return new SafariDriver(safariOptions);
                }

            case IE:
                InternetExplorerOptions internetExplorerOptions = new InternetExplorerOptions(desiredCapabilities);
                if (capabilities != null) {
                    capabilities.accept(internetExplorerOptions);
                }
                if (prop.getRemoteURL() != null) {
                    return getRemoteWebDriver(prop.getRemoteURL(), internetExplorerOptions);
                } else {
                    return new InternetExplorerDriver(internetExplorerOptions);
                }

            case EDGE:
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.merge(desiredCapabilities);
                if (capabilities != null) {
                    capabilities.accept(edgeOptions);
                }
                if (prop.getRemoteURL() != null) {
                    return getRemoteWebDriver(prop.getRemoteURL(), edgeOptions);
                } else {
                    return new EdgeDriver(edgeOptions);
                }

            case OPERA_BLINK:
                OperaOptions operaOptions = new OperaOptions();
                operaOptions.merge(desiredCapabilities);
                if (capabilities != null) {
                    capabilities.accept(operaOptions);
                }
                if (prop.getRemoteURL() != null) {
                    return getRemoteWebDriver(prop.getRemoteURL(), operaOptions);
                } else {
                    return new OperaDriver(operaOptions);
                }

        }
        return null;
    }

}