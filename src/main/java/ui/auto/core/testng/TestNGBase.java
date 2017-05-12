package ui.auto.core.testng;

import datainstiller.data.DataPersistence;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.events.MakeAttachmentEvent;
import ui.auto.core.support.TestContext;
import ui.auto.core.support.TestProperties;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Set;

@Listeners({AllureTestNGListener.class})
public class TestNGBase {
	private static final ThreadLocal<TestContext> context = new ThreadLocal<TestContext>() {
		@Override
		protected TestContext initialValue() {
			return new TestContext();
		}
	};
	private static final ThreadLocal<ITestContext> testNgContext = new ThreadLocal<>();
	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
	private long time;

	public static void takeScreenshot(String title) {
		if (context.get().getDriver() != null) {
			byte[] attachment = ((TakesScreenshot) context.get().getDriver()).getScreenshotAs(OutputType.BYTES);
			MakeAttachmentEvent ev = new MakeAttachmentEvent(attachment, title, "image/png");
			Allure.LIFECYCLE.fire(ev);
		}
	}

	public static void takeHTML(String title) {
		if (context.get().getDriver() != null) {
			byte[] attachment = (context.get().getDriver().getPageSource()).getBytes();
			MakeAttachmentEvent ev = new MakeAttachmentEvent(attachment, title, "text/html");
			Allure.LIFECYCLE.fire(ev);
		}
	}

	public static String getTestInfo() {
		String testInfo = testNgContext.get().getCurrentXmlTest().getName();
		testInfo += " " + testNgContext.get().getCurrentXmlTest().getTestParameters().toString();
		return testInfo;
	}

	public static TestContext CONTEXT() {
		return context.get();
	}

	public TestContext getContext() {
		if (context.get().getDriver() == null) {
			context.get().init();
			logInfo("+INITIALIZING CONTEXT: " + context.get().getDriver().toString());
		}
		return context.get();
	}

	@BeforeSuite
	public void initSuite(ITestContext testNgContext) {
		Integer threadCount = TestProperties.getInstance().getThreadCount();
		if (threadCount != null) {
			testNgContext.getSuite().getXmlSuite().setThreadCount(threadCount);
		}
	}

	@BeforeTest
	public void initTest(ITestContext testNgContext){
		if (TestNGBase.testNgContext.get() == null) {
			TestNGBase.testNgContext.set(testNgContext);
		}
		time = System.currentTimeMillis();
	}

	@AfterTest(alwaysRun = true)
	public void closeDriver(){
		time = (System.currentTimeMillis() - time) / 1000;
		if (context.get().getDriver() != null) {
			logInfo("-CLOSING CONTEXT: " + context.get().getDriver().toString());
			context.get().getDriver().quit();
		}
		context.remove();
		testNgContext.remove();
	}

	protected void setAttribute(String alias,Object value){
		testNgContext.get().getSuite().setAttribute(alias, value);
	}

	protected Object getAttribute(String alias){
		return testNgContext.get().getSuite().getAttribute(alias);
	}

	public void attachDataSet(DataPersistence data, String name) {
		if (context.get() != null && context.get().getDriver() != null) {
			byte[] attachment=data.toXML().getBytes();
			MakeAttachmentEvent ev=new MakeAttachmentEvent(attachment, name, "text/xml");
			Allure.LIFECYCLE.fire(ev);
		}
	}

	private StringBuilder getFailedConfigOrTests(Set<ITestResult> results) {
		StringBuilder log = new StringBuilder();
		log.append(" ---> \u001B[31mTEST FAILED :(\u001B[0m");
		log.append("\n");
		for (ITestResult result : results) {
			StringWriter stack = new StringWriter();
			result.getThrowable().printStackTrace(new PrintWriter(new PrintWriter(stack)));
			log.append("\n").append(stack);
		}
		return log;
	}

	private void logInfo(String msg) {
		StringBuilder log = new StringBuilder();
		String delim = "\n" + StringUtils.repeat("=", msg.length());
		log.append("ThreadId: ").append(Thread.currentThread().getId());
		log.append(delim);
		log.append("\nTEST: ").append(getTestInfo());
		log.append("\n").append(msg);
		if (msg.startsWith(("-CLOSING"))) {
			log.append("\nCOMPLETED AT:  " + new Date());
			log.append("\nTEST DURATION: ").append(time).append(" seconds");
			if (testNgContext.get().getFailedTests().size() > 0 || testNgContext.get().getFailedConfigurations().size() > 0) {
				log.append(getFailedConfigOrTests(testNgContext.get().getFailedConfigurations().getAllResults()));
				log.append(getFailedConfigOrTests(testNgContext.get().getFailedTests().getAllResults()));
			} else {
				log.append(" ---> \u001B[32mTEST PASSED :)\u001B[0m");
			}
		}
		log.append(delim).append("\n");
		LOG.info(log.toString());
	}


}
