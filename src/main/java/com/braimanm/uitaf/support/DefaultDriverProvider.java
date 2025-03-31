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
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.net.MalformedURLException;
import java.net.URL;

@SuppressWarnings("unused")
public class DefaultDriverProvider implements DriverProvider {

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
        capabilities.setBrowserName(getBrowserType().getDriverName());
        capabilities.setVersion(prop.getBrowserVersion());
        capabilities.setPlatform(platform);
        capabilities.merge(prop.getExtraCapabilities());
        return capabilities;
    }

    private URL getRemoteUrl() {
        TestProperties prop = TestContext.getTestProperties();
        URL url;
        try {
            url = new URL(prop.getRemoteURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException("webdriver.remote.url property has invalid value!");
        }
        return url;
    }

    @Override
    public WebDriver getNewDriverInstance() {
        TestProperties prop = TestContext.getTestProperties();
        DesiredCapabilities desiredCapabilities = getCapabilities();
        Proxy proxy = setupWebDriverProxy();
        if (proxy != null) {
            desiredCapabilities.setCapability(CapabilityType.PROXY, proxy);
        }
        if (prop.getAcceptSSLCerts()) {
            desiredCapabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
        }
        switch (getBrowserType()) {
            case FIREFOX:
                FirefoxProfile profile = new FirefoxProfile();
                profile.setPreference("focusmanager.testmode", true);
                FirefoxOptions firefoxOptions = new FirefoxOptions(desiredCapabilities);
                firefoxOptions.setProfile(profile);
                if (prop.getHeadless()) {
                    firefoxOptions.addArguments("-headless");
                }
                if (prop.getRemoteURL() != null) {
                    return getRemoteWebDriver(prop.getRemoteURL(), firefoxOptions);
                } else {
                    return new FirefoxDriver(firefoxOptions);
                }

            case CHROME:
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.merge(desiredCapabilities);
                chromeOptions.addArguments("--disable-notifications");
                if (prop.getHeadless()) {
                    chromeOptions.addArguments("--headless=new", "--disable-gpu");
                    String res = prop.getScreenSize();
                    if (res != null) {
                        res = res.toLowerCase().trim().replace("x", ",");
                        chromeOptions.addArguments("--window-size=" + res);
                    }
                }
                if (prop.getRemoteURL() != null) {
                    return getRemoteWebDriver(prop.getRemoteURL(), chromeOptions);
                } else {
                    return new ChromeDriver(chromeOptions);
                }

            case SAFARI:
                SafariOptions safariOptions = SafariOptions.fromCapabilities(desiredCapabilities);
                if (prop.getRemoteURL() != null) {
                    return getRemoteWebDriver(prop.getRemoteURL(), safariOptions);
                } else {
                    return new SafariDriver(safariOptions);
                }

            case IE:
                InternetExplorerOptions internetExplorerOptions = new InternetExplorerOptions(desiredCapabilities);
                if (prop.getRemoteURL() != null) {
                    return getRemoteWebDriver(prop.getRemoteURL(), internetExplorerOptions);
                } else {
                    return new InternetExplorerDriver(internetExplorerOptions);
                }

            case EDGE:
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.merge(desiredCapabilities);
                if (prop.getRemoteURL() != null) {
                    return getRemoteWebDriver(prop.getRemoteURL(), edgeOptions);
                } else {
                    return new EdgeDriver(edgeOptions);
                }

        }
        return null;
    }
}
