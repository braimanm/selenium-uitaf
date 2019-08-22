package ui.auto.core.support;

import org.openqa.selenium.remote.BrowserType;

public enum WebDriverTypeEnum {
    CHROME(BrowserType.CHROME),
    EDGE(BrowserType.EDGE),
    FIREFOX(BrowserType.FIREFOX),
    IE(BrowserType.IE),
    OPERA_BLINK(BrowserType.OPERA_BLINK),
    SAFARI(BrowserType.SAFARI),
    ANDROID(BrowserType.ANDROID),
    IPHONE(BrowserType.IPHONE),
    IPAD(BrowserType.IPAD);

    String driverName;

    WebDriverTypeEnum(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverName() {
        return driverName;
    }



}