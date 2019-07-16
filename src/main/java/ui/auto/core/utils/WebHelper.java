package ui.auto.core.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ui.auto.core.pagecomponent.PageComponent;
import ui.auto.core.support.TestContext;
import ui.auto.core.support.TestProperties;
import ui.auto.core.testng.TestNGBase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WebHelper {

    public static WebDriver getWebDriver() {
        return TestNGBase.CONTEXT().getDriver();
    }

    public static WebDriverWait getWebDriiverWait() {
        TestProperties props = TestContext.getTestProperties();
        return new WebDriverWait(getWebDriver(), props.getElementTimeout());
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
        waitForXHR(props.getElementTimeout() * 1000, 500, false);
    }

    public static void waitForXHR(long timeout, long sleep, boolean debug) {
        String script = "function reqCallBack(t){document.getElementsByTagName('body')[0].setAttribute('ajaxcounter',++ajaxCount)}function resCallback(t){document.getElementsByTagName('body')[0].setAttribute('ajaxcounter',--ajaxCount)}function intercept(){XMLHttpRequest.prototype.send=function(){if(reqCallBack(this),this.addEventListener){var t=this;this.addEventListener('readystatechange',function(){4===t.readyState&&resCallback(t)},!1)}else{var e=this.onreadystatechange;e&&(this.onreadystatechange=function(){4===t.readyState&&resCallbck(this),e()})}originalXhrSend.apply(this,arguments)}}var originalXhrSend=XMLHttpRequest.prototype.send,ajaxCount=0;document.getElementsByTagName('body')[0].hasAttribute('ajaxcounter')||intercept();";
        JavascriptExecutor driver = (JavascriptExecutor) getWebDriver();
        driver.executeScript(script);

        long to = System.currentTimeMillis() + timeout;
        boolean flag = true;
        if (debug) System.out.print("XHR: ");
        do {
            String val = getWebDriver().findElement(By.cssSelector("body")).getAttribute("ajaxcounter");
            if (val == null) {
                val = "-1";
                if (System.currentTimeMillis() > (to - timeout + 2000)) {
                    if (debug) System.out.println();
                    return;
                }
            }
            if (debug) System.out.print(val + " ");
            if (Integer.valueOf(val) == 0) {
                flag = false;
            }
            if (flag) sleep(sleep);
        } while (flag && System.currentTimeMillis() < to);
        if (debug) System.out.println();
    }

}