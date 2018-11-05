package ui.auto.core.support;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import ui.auto.core.context.PageComponentContext;

import java.util.function.Consumer;


@SuppressWarnings("unused")
public class TestContext extends PageComponentContext {
	private static ThreadLocal<TestProperties> props = ThreadLocal.withInitial(TestProperties::new);

	public TestContext(WebDriver driver) {
		super(driver);
		setTimeouts();
	}

	public TestContext() {
		this(null);
	}

	public static TestProperties getTestProperties() {
		return props.get();
	}

    public void init(Consumer<MutableCapabilities> capabilities) {
        if (driver != null) return;
        driver = getTestProperties().getBrowserType().getNewWebDriver(capabilities);
		String res = getTestProperties().getScreenSize();
		if (res != null) {
			String[] resWH = res.toLowerCase().split("x");
			int width = Integer.parseInt(resWH[0].trim());
			int height = Integer.parseInt(resWH[1].trim());
			Dimension dim = new Dimension(width, height);
			driver.manage().window().setSize(dim);
		}
	}

	public String getAlias(String key) {
		return getGlobalAliases().get(key);
	}

	public void setAlias(String key,String value) {
		getGlobalAliases().put(key, value);
	}


	private void setTimeouts() {
		if (getTestProperties().getElementTimeout()>0) {
			setAjaxTimeOut(getTestProperties().getElementTimeout());
		}
		if (getTestProperties().getPageTimeout()>0) {
			setWaitForUrlTimeOut(getTestProperties().getPageTimeout() * 1000);
		}
	}

}