/*
Copyright 2010-2019 Michael Braiman braimanm@gmail.com
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package ui.auto.core.testng;

import datainstiller.data.DataAliases;
import datainstiller.data.DataPersistence;
import io.appium.java_client.MobileDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.events.MakeAttachmentEvent;
import ui.auto.core.context.PageComponentContext;
import ui.auto.core.support.TestContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"unused", "SameParameterValue"})
@Listeners({AllureTestNGListener.class})
public class TestNGBase {
	private static final ThreadLocal<TestContext> context = ThreadLocal.withInitial(TestContext::new);
	private static final ThreadLocal<ITestContext> testNgContext = new ThreadLocal<>();
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	private long time;

    public synchronized static TestContext CONTEXT() {
        return context.get();
    }

	public synchronized static void takeScreenshot(String title) {
        if (CONTEXT().getDriver() != null) {
        	try {
				byte[] attachment = ((TakesScreenshot) CONTEXT().getDriver()).getScreenshotAs(OutputType.BYTES);
				MakeAttachmentEvent ev = new MakeAttachmentEvent(attachment, title, "image/png");
				Allure.LIFECYCLE.fire(ev);
			} catch (Exception ignore) {}
		}
	}

	public synchronized static void takeHTML(String title) {
        if (CONTEXT().getDriver() != null) {
        	if (!MobileDriver.class.isAssignableFrom(CONTEXT().getDriver().getClass())) {
        		try {
					byte[] attachment = (CONTEXT().getDriver().getPageSource()).getBytes();
					MakeAttachmentEvent ev = new MakeAttachmentEvent(attachment, title, "text/html");
					Allure.LIFECYCLE.fire(ev);
				} catch (Exception ignore) {}
			}
		}
	}

	public TestContext getContext() {
        if (CONTEXT().getDriver() == null) {
            CONTEXT().init();
			String driverInfo = CONTEXT().getDriver().toString();
			setAttribute("driver-info", driverInfo);
			logInfo("+INITIALIZING CONTEXT: " + driverInfo );
        }
        return CONTEXT();
    }

	public synchronized static String getTestInfo() {
		String testInfo = testNgContext.get().getCurrentXmlTest().getName();
		testInfo += " " + testNgContext.get().getCurrentXmlTest().getLocalParameters().toString();
		return testInfo;
	}

	private synchronized static String resolveAliases(DataPersistence data) {
		if (data.getDataAliases() != null) {
			data.getDataAliases().clear();
		}
		DataAliases aliases = PageComponentContext.getGlobalAliases();
		String xml = data.toXML().replace("<aliases/>","");
		for (String key : aliases.keySet()){
			String alias = "${" + key + "}";
			String value = aliases.getAsString(key);
			xml = xml.replace(alias, value);
		}
		return xml;
	}

	public synchronized static void attachDataSet(DataPersistence data, String name) {
		byte[] attachment =  resolveAliases(data).getBytes();
		MakeAttachmentEvent ev = new MakeAttachmentEvent(attachment, name, "text/xml");
		Allure.LIFECYCLE.fire(ev);
	}

	protected void installDrivers() {
		List<DriverManagerType> drivers = TestContext.getTestProperties().getDriversToInstall();
		drivers.forEach(driver -> WebDriverManager.getInstance(driver).setup());
	}

	@BeforeSuite
	public void initSuite(ITestContext testNgContext) {
		if (TestContext.getTestProperties().installDrivers()) installDrivers();
		Integer threadCount = TestContext.getTestProperties().getThreadCount();
		if (threadCount != null) {
			testNgContext.getSuite().getXmlSuite().setThreadCount(threadCount);
		}
	}

    protected void initTest(ITestContext testNgContext) {
        time = System.currentTimeMillis();
        if (TestNGBase.testNgContext.get() == null || TestNGBase.testNgContext.get() != testNgContext) {
            TestNGBase.testNgContext.set(testNgContext);
        }
    }

	protected void closeDriver() {
		if (CONTEXT().getDriver() != null) {
			CONTEXT().getDriver().quit();
		}
		context.remove();
	}

	private void closeDriverAfterTest() {
		time = (System.currentTimeMillis() - time) / 1000;
		String driverInfo = getAttribute("driver-info");
		logInfo("-CLOSING CONTEXT: " + driverInfo);
		closeDriver();
	}

	@BeforeTest
	public void beforeTest(ITestContext testNgContext) {
		String parallel = testNgContext.getSuite().getParallel();
		if (parallel.equals("tests") || parallel.equals("none")) initTest(testNgContext);
	}

	@AfterTest
	public void afterTest(ITestContext testNgContext) {
		String parallel = testNgContext.getSuite().getParallel();
		if (parallel.equals("tests") || parallel.equals("none")) closeDriverAfterTest();
	}

	@BeforeMethod
	public void beforeMethod(ITestContext testNgContext) {
		if (testNgContext.getSuite().getParallel().equals("methods")) initTest(testNgContext);
	}

	@AfterMethod
	public void afterMethod(ITestContext testNgContext) {
		if (testNgContext.getSuite().getParallel().equals("methods")) closeDriverAfterTest();
	}

	@BeforeClass
	public void beforeClass(ITestContext testNgContext) {
		String parallel = testNgContext.getSuite().getParallel();
		if (parallel.equals("classes") || parallel.equals("instances")) initTest(testNgContext);
	}

	@AfterClass
	public void afterClass(ITestContext testNgContext) {
		String parallel = testNgContext.getSuite().getParallel();
		if (parallel.equals("classes") || parallel.equals("instances")) closeDriverAfterTest();
	}

	protected void setAttribute(String alias, Object value){
		testNgContext.get().getSuite().setAttribute(alias, value);
	}

	protected String getAttribute(String alias){
		Object o = testNgContext.get().getSuite().getAttribute(alias);
		return (o == null) ? "Not Found" : o.toString();
	}

	private StringBuilder getFailedConfigOrTests(Set<ITestResult> results) {
		StringBuilder log = new StringBuilder();
		for (ITestResult result : results) {
			StringWriter stack = new StringWriter();
			result.getThrowable().printStackTrace(new PrintWriter(new PrintWriter(stack)));
			log.append("\n").append(stack);
		}
		return log;
	}

	private void logInfo(String msg) {
		StringBuilder log = new StringBuilder();
		String delimit = "\n" + StringUtils.repeat("=", msg.length());
		log.append("ThreadId: ").append(Thread.currentThread().getId());
		log.append(delimit);
		log.append("\nTEST: ").append(getTestInfo());
		log.append("\n").append(msg);
		if (msg.startsWith(("-CLOSING"))) {
			log.append("\nCOMPLETED AT:  ").append(new Date());
			log.append("\nTEST DURATION: ").append(time).append(" seconds");
			boolean testsArePassed = testNgContext.get().getPassedTests().size() > 0;
			boolean testsAreFailed = testNgContext.get().getFailedTests().size() > 0;
			boolean configsAreFailed = testNgContext.get().getFailedConfigurations().size() > 0;
			if (testsAreFailed || configsAreFailed || !testsArePassed) {
				log.append(" ---> \u001B[31mTEST FAILED :(\u001B[0m\n");
				log.append(getFailedConfigOrTests(testNgContext.get().getFailedConfigurations().getAllResults()));
				log.append(getFailedConfigOrTests(testNgContext.get().getFailedTests().getAllResults()));
			} else {
				log.append(" ---> \u001B[32mTEST PASSED :)\u001B[0m");
			}
		}
		log.append(delimit).append("\n");
		LOG.info(log.toString());
	}

}