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

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.qameta.allure.internal.shadowed.jackson.core.JsonProcessingException;
import io.qameta.allure.internal.shadowed.jackson.databind.ObjectMapper;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@SuppressWarnings("unused")
public class DefaultDriverProvider implements DriverProvider {

    protected Proxy setupWebDriverProxy() {
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

    protected URL getRemoteUrl(String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException("webdriver.remote.url property has invalid value!");
        }
    }

    protected void addScreenSize(TestProperties prop, Object options) {
        String res = prop.getScreenSize();
        if (res != null) {
            res = res.toLowerCase().trim().replace("x", ",");
            String arg = "--window-size=" + res;
            if (options instanceof ChromeOptions) {
                ((ChromeOptions) options).addArguments(arg);
            } else if (options instanceof FirefoxOptions) {
                ((FirefoxOptions) options).addArguments(arg);
            } else if (options instanceof EdgeOptions) {
                ((EdgeOptions) options).addArguments(arg);
            }
        }
    }

    protected static WebDriver createAppiumDriver(String json) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> config;
        try {
            config = mapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // Extract and remove server URL from config
        String appiumServerUrl = (String) config.remove("appiumServerUrl");
        if (appiumServerUrl == null) {
            throw new IllegalArgumentException("Missing 'appiumServerUrl' in JSON config.");
        }

        DesiredCapabilities capabilities = new DesiredCapabilities();
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            capabilities.setCapability(entry.getKey(), entry.getValue());
        }

        String platformName = (String) config.get("platformName");
        URL serverUrl;
        try {
            serverUrl = new URL(appiumServerUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        if ("iOS".equalsIgnoreCase(platformName)) {
            return new IOSDriver(serverUrl, capabilities);
        } else if ("Android".equalsIgnoreCase(platformName)) {
            return new AndroidDriver(serverUrl, capabilities);
        } else {
            throw new IllegalArgumentException("Unsupported platform: " + platformName);
        }
    }

    @Override
    public WebDriver getNewDriverInstance(TestProperties prop) {
        String browserName = prop.getBrowserType().toLowerCase();
        String remoteUrl = prop.getRemoteURL();
        boolean isRemote = remoteUrl != null && !remoteUrl.isBlank();

        Proxy proxy = setupWebDriverProxy();
        boolean acceptSSLCerts = prop.getAcceptSSLCerts();
        boolean headless = prop.getHeadless();

        switch (prop.getBrowserType().toUpperCase()) {
            case "FIREFOX":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (proxy != null) firefoxOptions.setProxy(proxy);
                if (acceptSSLCerts) firefoxOptions.setAcceptInsecureCerts(true);
                if (headless) firefoxOptions.addArguments("-headless");
                addScreenSize(prop, firefoxOptions);
                if (isRemote) {
                    return new RemoteWebDriver(getRemoteUrl(remoteUrl), firefoxOptions);
                } else {
                    return new FirefoxDriver(firefoxOptions);
                }

            case "CHROME":
                ChromeOptions chromeOptions = new ChromeOptions();
                if (proxy != null) chromeOptions.setProxy(proxy);
                if (acceptSSLCerts) chromeOptions.setAcceptInsecureCerts(true);
                chromeOptions.addArguments("--disable-notifications");
                if (headless) {
                    chromeOptions.addArguments("--headless=new", "--disable-gpu");
                }
                addScreenSize(prop, chromeOptions);
                if (isRemote) {
                    return new RemoteWebDriver(getRemoteUrl(remoteUrl), chromeOptions);
                } else {
                    return new ChromeDriver(chromeOptions);
                }

            case "SAFARI":
                SafariOptions safariOptions = new SafariOptions();
                if (proxy != null) safariOptions.setProxy(proxy);
                if (acceptSSLCerts) safariOptions.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
                if (isRemote) {
                    return new RemoteWebDriver(getRemoteUrl(remoteUrl), safariOptions);
                } else {
                    return new SafariDriver(safariOptions);
                }

            case "EDGE":
                EdgeOptions edgeOptions = new EdgeOptions();
                if (proxy != null) edgeOptions.setProxy(proxy);
                if (acceptSSLCerts) edgeOptions.setAcceptInsecureCerts(true);
                if (headless) {
                    edgeOptions.addArguments("--headless=new", "--disable-gpu");
                }
                addScreenSize(prop, edgeOptions);
                if (isRemote) {
                    return new RemoteWebDriver(getRemoteUrl(remoteUrl), edgeOptions);
                } else {
                    return new EdgeDriver(edgeOptions);
                }

            case "IOS":
            case "ANDROID":
                return createAppiumDriver(prop.getExtraCapabilities());

            default:
                throw new IllegalArgumentException("Unsupported browser: " + prop.getBrowserType());

        }


    }
}

