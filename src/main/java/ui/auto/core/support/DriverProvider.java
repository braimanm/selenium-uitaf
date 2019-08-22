package ui.auto.core.support;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

public interface DriverProvider {

    default WebDriverTypeEnum getBrowserType() {
        return TestContext.getTestProperties().getBrowserType();
    }

    default WebDriver getRemoteWebDriver(String url, MutableCapabilities capabilities) {
        try {
            return new RemoteWebDriver(new URL(url), capabilities);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL!", e);
        }
    }

    WebDriver getNewDriverInstance();
}
