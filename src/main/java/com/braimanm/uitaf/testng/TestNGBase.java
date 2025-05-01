/*
Copyright 2010-2024 Michael Braiman braimanm@gmail.com
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

package com.braimanm.uitaf.testng;

import com.braimanm.datainstiller.data.DataPersistence;
import com.braimanm.ui.auto.context.WebDriverContext;
import com.braimanm.uitaf.support.TestContext;
import io.qameta.allure.Allure;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Set;

@SuppressWarnings({"unused", "SameParameterValue"})
@Listeners(AllureTestNGListener.class)
public class TestNGBase {
	private static final ThreadLocal<ITestContext> testNgContext = new ThreadLocal<>();
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	private long time;

	public WebDriverContext getContext(String contextName) {
		if (TestContext.getContext(contextName) == null) {
			TestContext.init(contextName);
			String driverInfo = "[" + contextName + "]: " + TestContext.getContext(contextName).getDriver().toString();
			setAttribute("driver-info", driverInfo);
			logInfo("+INITIALIZING CONTEXT: " + driverInfo );
		}
		return TestContext.getContext(contextName);
	}

	public WebDriverContext getContext() {
		return getContext(TestContext.DEFAULT);
	}

	protected void closeDriver() {
		TestContext.removeContext();
	}

	protected void closeDriver(String contextName) {
		TestContext.removeContext(contextName);
	}

	public synchronized static void takeScreenshot(String contextName, String title) {
        if (TestContext.getContext(contextName) != null) {
        	try {
				TakesScreenshot takesScreenshot = (TakesScreenshot) TestContext.getContext(contextName).getDriver();
				byte[] attachment = takesScreenshot.getScreenshotAs(OutputType.BYTES);
				Allure.getLifecycle().addAttachment(title, "image/png", "png", attachment);
			} catch (Exception ignore) {}
		}
	}

	public static void takeScreenshot(String title) {
		takeScreenshot(TestContext.DEFAULT, title);
	}


	public synchronized static String getTestInfo() {
		String testInfo = testNgContext.get().getCurrentXmlTest().getName();
		testInfo += " " + testNgContext.get().getCurrentXmlTest().getLocalParameters().toString();
		return testInfo;
	}

	public synchronized static void attachDataSet(DataPersistence data, String name, Boolean resolveAliases) {
		byte[] attachment;
		if (resolveAliases) {
			data.getDataAliases().clear();
			attachment = TestContext.getGlobalAliases().resolveAliases(data).getBytes();
		} else {
			attachment = data.toXML().getBytes();
		}
		Allure.getLifecycle().addAttachment(name, "text/xml", "xml", attachment);
	}

	public synchronized static void attachDataSet(DataPersistence data, String name) {
		attachDataSet(data, name, false);
	}

    protected void initTest(ITestContext testNgContext) {
        time = System.currentTimeMillis();
        if (TestNGBase.testNgContext.get() == null || TestNGBase.testNgContext.get() != testNgContext) {
            TestNGBase.testNgContext.set(testNgContext);
        }
    }

	private void closeDriverAfterTest() {
		time = (System.currentTimeMillis() - time) / 1000;
		String driverInfo = getAttribute("driver-info");
		for (String contextName : TestContext.getAllContextNames()) {
			logInfo("-CLOSING CONTEXT[" + contextName + "]:" + driverInfo);
			closeDriver(contextName);
		}
	}

	@BeforeTest(alwaysRun = true)
	public void _beforeTest_(ITestContext testNgContext) {
		String parallel = testNgContext.getSuite().getParallel();
		if (parallel.equals("tests") || parallel.equals("none")) {
			initTest(testNgContext);
		}
	}

	@AfterTest(alwaysRun = true)
	public void _afterTest_(ITestContext testNgContext) {
		String parallel = testNgContext.getSuite().getParallel();
		if (parallel.equals("tests") || parallel.equals("none")) {
			closeDriverAfterTest();
		}
	}

	@BeforeMethod(alwaysRun = true)
	public void _beforeMethod_(ITestContext testNgContext) {
		if (testNgContext.getSuite().getParallel().equals("methods")) {
			initTest(testNgContext);
		}
	}

	@AfterMethod(alwaysRun = true)
	public void _afterMethod_(ITestContext testNgContext) {
		if (testNgContext.getSuite().getParallel().equals("methods")) {
			closeDriverAfterTest();
		}
	}

	@BeforeClass(alwaysRun = true)
	public void _beforeClass_(ITestContext testNgContext) {
		String parallel = testNgContext.getSuite().getParallel();
		if (parallel.equals("classes") || parallel.equals("instances")) {
			initTest(testNgContext);
		}
	}

	@AfterClass(alwaysRun = true)
	public void _afterClass_(ITestContext testNgContext) {
		String parallel = testNgContext.getSuite().getParallel();
		if (parallel.equals("classes") || parallel.equals("instances")) {
			closeDriverAfterTest();
		}
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