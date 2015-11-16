package ui.auto.core.testng;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import datainstiller.data.DataPersistence;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.events.MakeAttachmentEvent;
import ui.auto.core.support.TestContext;

public class TestNGBase {
	protected static TestContext context;
	private ITestContext testNgContext;
	
	@BeforeTest
	public void initTest(ITestContext testNgContext){
		this.testNgContext = testNgContext;
		context=new TestContext();
		testNgContext.setAttribute("context", context);// This is needed for AllureTestNGListener to be able to create screenshot of failed test
	}

	
	@AfterTest(alwaysRun=true)
	public void closeDriver(){
		if (context!=null){
			context.getDriver().close();
			context.getDriver().quit();
		}
	}
	
	
	protected void setAttribute(String alias,Object value){
		testNgContext.getSuite().setAttribute(alias, value);
	}
	
	protected Object getAttribute(String alias){
		return testNgContext.getSuite().getAttribute(alias);
	}
	
	public void attachDataSet(DataPersistence data,String name){
		if (context!=null && context.getDriver()!=null) {
			byte[] attachment=data.toXML().getBytes();
			MakeAttachmentEvent ev=new MakeAttachmentEvent(attachment, name, "text/xml");
		   	Allure.LIFECYCLE.fire(ev);
		}
	}
	
	public void attachScreenShoot() {
		if (context!=null && context.getDriver()!=null) {
			byte[] attachment=((TakesScreenshot) context.getDriver()).getScreenshotAs(OutputType.BYTES);
			MakeAttachmentEvent ev=new MakeAttachmentEvent(attachment, "Screenshot", "image/png");
		   	Allure.LIFECYCLE.fire(ev);
		}
	}

	
}