package ui.auto.core.testng;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.events.TestCaseFailureEvent;
import ru.yandex.qatools.allure.events.TestCaseFinishedEvent;
import ru.yandex.qatools.allure.testng.AllureTestListener;
import ui.auto.core.support.TestContext;


public class AllureTestNGListener extends AllureTestListener {
	private Allure lifecycle = Allure.LIFECYCLE;

	@Override
	public void onStart(ITestContext iTestContext) {
		super.onStart(iTestContext);
		for (ITestNGMethod method : iTestContext.getAllTestMethods()) {
			Retry retry = method.getConstructorOrMethod().getMethod().getAnnotation(Retry.class);
			if (isCandidateForRetry(method) && method.getRetryAnalyzer() == null && retry != null) {
				int times = (retry.value() == 0) ? TestContext.getTestProperties().getTestDefaultRetry() : retry.value();
				method.setRetryAnalyzer(new RetryListener(times));
			}
		}
	}

	@Override
	public void onTestFailure(ITestResult iTestResult) {
		lifecycle.fire(new TestCaseFailureEvent().withThrowable(iTestResult.getThrowable()));
		TestNGBase.takeScreenshot("Failed Test Screenshot");
		TestNGBase.takeHTML("Failed Test HTML Source");
		lifecycle.fire(new TestCaseFinishedEvent());
	}

	@Override
	public void onTestSkipped(ITestResult iTestResult) {
		TestNGBase.takeScreenshot("Failed Test Screenshot");
		TestNGBase.takeHTML("Failed Test HTML Source");
		super.onTestSkipped(iTestResult);
	}

	private boolean isCandidateForRetry(ITestNGMethod method) {
		// Annotation:  @Test
		if (method.isTest()) {
			return true;
		}

		// Annotation:  @BeforeTest
		if (method.isBeforeTestConfiguration()) {
			return true;
		}

		// Annotation:  @AfterTest
		if (method.isAfterTestConfiguration()) {
			return true;
		}

		// Annotation:  @BeforeMethod
		if (method.isBeforeMethodConfiguration()) {
			return true;
		}

		// Annotation:  @AfterMethod
		return method.isAfterMethodConfiguration();

	}


}