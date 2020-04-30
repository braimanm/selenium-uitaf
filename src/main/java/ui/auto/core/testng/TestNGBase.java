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
import ui.auto.core.utils.WebDriverInstaller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Set;

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
            logInfo("+INITIALIZING CONTEXT: " + CONTEXT().getDriver().toString());
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
			String value = aliases.get(key);
			xml = xml.replace(alias, value);
		}
		return xml;
	}

	public synchronized static void attachDataSet(DataPersistence data, String name) {
		byte[] attachment =  resolveAliases(data).getBytes();
		MakeAttachmentEvent ev = new MakeAttachmentEvent(attachment, name, "text/xml");
		Allure.LIFECYCLE.fire(ev);
	}

	private void setUpDrivers() {
		WebDriverInstaller installer = new WebDriverInstaller();
		installer.installDriver("geckodriver", "webdriver.gecko.driver");
		installer.installDriver("chromedriver", "webdriver.chrome.driver");
	}


	@BeforeSuite
	public void initSuite(ITestContext testNgContext) {
		if (TestContext.getTestProperties().installDrivers()) setUpDrivers();
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

    protected void closeDriver(ITestContext testNgContext) {
        time = (System.currentTimeMillis() - time) / 1000;
        if (CONTEXT().getDriver() != null) {
            logInfo("-CLOSING CONTEXT: " + CONTEXT().getDriver().toString());
            CONTEXT().getDriver().quit();
        }
        context.remove();
    }

    @BeforeTest
    public void beforeTest(ITestContext testNgContext) {
        String parallel = testNgContext.getSuite().getParallel();
        if (parallel.equals("tests") || parallel.equals("none")) initTest(testNgContext);
    }

    @AfterTest
    public void afterTest(ITestContext testNgContext) {
        String parallel = testNgContext.getSuite().getParallel();
        if (parallel.equals("tests") || parallel.equals("none")) closeDriver(testNgContext);
    }

    @BeforeMethod
    public void beforeMethod(ITestContext testNgContext) {
        if (testNgContext.getSuite().getParallel().equals("methods")) initTest(testNgContext);
    }

    @AfterMethod
    public void afterMethod(ITestContext testNgContext) {
        if (testNgContext.getSuite().getParallel().equals("methods")) closeDriver(testNgContext);
    }

    @BeforeClass
    public void beforeClass(ITestContext testNgContext) {
        String parallel = testNgContext.getSuite().getParallel();
        if (parallel.equals("classes") || parallel.equals("instances")) initTest(testNgContext);
    }

    @AfterClass
    public void afterClass(ITestContext testNgContext) {
        String parallel = testNgContext.getSuite().getParallel();
        if (parallel.equals("classes") || parallel.equals("instances")) closeDriver(testNgContext);
    }

	protected void setAttribute(String alias,Object value){
		testNgContext.get().getSuite().setAttribute(alias, value);
	}

	protected Object getAttribute(String alias){
		return testNgContext.get().getSuite().getAttribute(alias);
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
			log.append("\nCOMPLETED AT:  ").append(new Date());
			log.append("\nTEST DURATION: ").append(time).append(" seconds");
			boolean testsArePassed = testNgContext.get().getPassedTests().size() > 0;
			boolean testsAreFailed = testNgContext.get().getFailedTests().size() > 0;
			boolean configsAreFailed = testNgContext.get().getFailedConfigurations().size() > 0;
			if (testsAreFailed || configsAreFailed || !testsArePassed) {
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