package ui.auto.core.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import ui.auto.core.components.WebComponent;
import ui.auto.core.pagecomponent.PageComponent;
import ui.auto.core.support.TestContext;
import ui.auto.core.support.TestProperties;
import ui.auto.core.testng.TestNGBase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class Helper {

    public static WebDriver getWebDriver() {
        return TestNGBase.CONTEXT().getDriver();
    }

    public static WebDriverWait getWebDriiverWait() {
        TestProperties props = TestContext.getTestProperties();
        return new WebDriverWait(getWebDriver(), props.getElementTimeout());
    }

    public static FluentWait<WebDriver> getFluentWait() {
        TestProperties props = TestContext.getTestProperties();
        return new FluentWait<>(getWebDriver()).withTimeout(props.getElementTimeout(), TimeUnit.SECONDS);
    }

    public static FluentWait<WebDriver> getFluentWait(int timeOutInSeconds) {
        return new FluentWait<>(getWebDriver()).withTimeout(timeOutInSeconds, TimeUnit.SECONDS);
    }


    public static boolean waitToShow(PageComponent component, Integer timeOutInSeconds) {
        FluentWait<WebDriver> wait;
        if (timeOutInSeconds == null) {
            wait = getFluentWait();
        } else {
            wait = getFluentWait(timeOutInSeconds);
        }
        try {
            wait.ignoring(NoSuchElementException.class)
                    .until(ExpectedConditions.visibilityOfElementLocated(component.getLocator()));
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    public static boolean waitToShow(PageComponent component) {
        return waitToShow(component, null);
    }


    public static boolean waitToShow(WebElement webElement) {
        FluentWait<WebDriver> wait = getFluentWait().ignoring(NoSuchElementException.class);
        try {
            wait.until(ExpectedConditions.visibilityOf(webElement));
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    public static void waitToHide(By by) {
        getWebDriiverWait().until(ExpectedConditions.invisibilityOfElementLocated(by));
    }

    public static void waitToHide(WebComponent component) {
        getWebDriiverWait().until(ExpectedConditions.invisibilityOfElementLocated(component.getLocator()));
    }

    public static WebElement waitToShow(By by) {
        FluentWait<WebDriver> wait = getFluentWait();
        return wait.ignoring(NoSuchElementException.class).until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static WebElement waitToShow(WebElement parent, By child) {
        FluentWait<WebDriver> wait = getFluentWait();
        return wait.ignoring(NoSuchElementException.class).until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(parent, child)).get(0);
    }

    private static Function<WebDriver, Boolean> isAjaxReady() {

        return webDriver -> {
            JavascriptExecutor driver = (JavascriptExecutor) webDriver;
            long ang = (long) driver.executeScript("return window.angular.element('body').injector().get('$http').pendingRequests.length;");
            long jq = (long) driver.executeScript("return window.jQuery.active;");
            return ((ang + jq) == 0);
        };
    }

    //This method will not throw exception during validation
    public static boolean isDispalyed(PageComponent component) {
        List<WebElement> elList = getWebDriver().findElements(component.getLocator());
        if (elList.isEmpty()) {
            return false;
        }
        return elList.get(0).isDisplayed();
    }

    public static void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {}
    }

    public static void moveFocusToElement(WebElement element) {
        Actions actions = new Actions(getWebDriver());
        actions.moveToElement(element).build().perform();
    }

    public static String convertDate(String patternFrom, String patternTo, String dateValue) {
        String dateOut = dateValue;
        try {
            Date date = new SimpleDateFormat(patternFrom).parse(dateValue);
            dateOut = new SimpleDateFormat(patternTo).format(date);
        } catch (ParseException ignore) {}
        return dateOut;
    }

    public static void waitForXHR() {
        TestProperties props = TestContext.getTestProperties();
        waitForXHR(props.getElementTimeout() * 1000, 500);
    }

    public static void waitForXHR(long timeout, long sleep) {
        String script = "function reqCallBack(t){document.getElementsByTagName('body')[0].setAttribute('ajaxcounter',++ajaxCount)}function resCallback(t){document.getElementsByTagName('body')[0].setAttribute('ajaxcounter',--ajaxCount)}function intercept(){XMLHttpRequest.prototype.send=function(){if(reqCallBack(this),this.addEventListener){var t=this;this.addEventListener('readystatechange',function(){4===t.readyState&&resCallback(t)},!1)}else{var e=this.onreadystatechange;e&&(this.onreadystatechange=function(){4===t.readyState&&resCallbck(this),e()})}originalXhrSend.apply(this,arguments)}}var originalXhrSend=XMLHttpRequest.prototype.send,ajaxCount=0;document.getElementsByTagName('body')[0].hasAttribute('ajaxcounter')||intercept();";
        JavascriptExecutor driver = (JavascriptExecutor) getWebDriver();
        driver.executeScript(script);

        long to = System.currentTimeMillis() + timeout;
        boolean flag = true;
        System.out.print("XHR: ");
        do {
            String val = getWebDriver().findElement(By.cssSelector("body")).getAttribute("ajaxcounter");
            if (val == null) {
                val = "-1";
                if (System.currentTimeMillis() > (to - timeout + 2000)) {
                    System.out.println();
                    return;
                }
            }
            System.out.print(val + " ");
            if (Integer.valueOf(val) == 0) {
                flag = false;
            }
            if (flag) sleep(sleep);
        } while (flag && System.currentTimeMillis() < to);
        System.out.println();
    }

}