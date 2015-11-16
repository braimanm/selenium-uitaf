package ui.auto.core.testng;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;

import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.events.MakeAttachmentEvent;
import ru.yandex.qatools.allure.events.TestCaseFailureEvent;
import ru.yandex.qatools.allure.events.TestCaseFinishedEvent;
import ru.yandex.qatools.allure.testng.AllureTestListener;
import ui.auto.core.support.TestContext;

public class AllureTestNGListener extends AllureTestListener{	
	 private Allure lifecycle = Allure.LIFECYCLE;
	
	 @Override
	 public void onTestFailure(ITestResult iTestResult) {
		 lifecycle.fire(new TestCaseFailureEvent().withThrowable(iTestResult.getThrowable()));
		 takeScreenShot(iTestResult);
		 lifecycle.fire(new TestCaseFinishedEvent());
	}

	private void takeScreenShot(ITestResult iTestResult){
		TestContext context=(TestContext) iTestResult.getTestContext().getAttribute("context");
		if (context!=null && context.getDriver()!=null) {
			byte[] attachment=((TakesScreenshot) context.getDriver()).getScreenshotAs(OutputType.BYTES);
			MakeAttachmentEvent ev=new MakeAttachmentEvent(attachment, "Failed Test Screenshot", "image/png");
		   	lifecycle.fire(ev);
		}
	}

}
