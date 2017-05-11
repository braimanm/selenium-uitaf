package ui.auto.core.support;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import ui.auto.core.context.PageComponentContext;

public class TestContext extends PageComponentContext {
	private TestProperties props = TestProperties.getInstance(); 
	
	public TestContext(WebDriver driver) {
		super(driver);
		setTimeouts();
	}

	public TestContext() {
		super(null);
	}

	public void init() {
		driver = props.getBrowserType().getNewWebDriver();
		String res = props.getScreenSize();
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


	protected void setTimeouts() {
		if (props.getElementTimeout()>0) {
			setAjaxTimeOut(props.getElementTimeout());
		}
		if (props.getPageTimeout()>0) {
			setWaitForUrlTimeOut(props.getPageTimeout());
		}
	}

}
