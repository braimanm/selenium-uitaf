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

		return method.isTest() ||
				method.isBeforeTestConfiguration() ||
				method.isAfterTestConfiguration() ||
				method.isBeforeMethodConfiguration() ||
				method.isAfterMethodConfiguration();

	}


}