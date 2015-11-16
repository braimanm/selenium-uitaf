package ui.auto.core.support;

import java.net.URL;
import java.net.URLConnection;

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
		driver = props.getBrowserType().getNewWebDriver();
		setTimeouts();
		isWebLive();
	}
	
	public String getAlias(String key) {
		return (String) getGlobalAliases().get(key);
	}
	
	public void setAlias(String key,String value) {
		getGlobalAliases().put(key, value);
	}
	
	
	private void setTimeouts(){
		if (props.getElementTimeout()>0) {
			setAjaxTimeOut(props.getElementTimeout());
		}
		if (props.getPageTimeout()>0) {
			setWaitForUrlTimeOut(props.getPageTimeout());
		}
	}
	
	private void isWebLive(){
		String surl = props.getAutURL();
		if (surl == null) {
			return;
		}
		try {
			URL url=new URL(surl);
			URLConnection urlConn=url.openConnection();
			urlConn.setConnectTimeout(props.getPageTimeout());
			urlConn.connect();
		} catch (Exception e) {
			if (super.getDriver() != null) {
				super.getDriver().quit();
			}
			throw new  RuntimeException("Url \"" + surl + "\" is not responding!",e);
		}
		driver.get(surl);
	}
	
	
}
