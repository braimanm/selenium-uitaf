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

package com.braimanm.uitaf.support;

import com.braimanm.ui.auto.context.PageComponentContext;
import org.openqa.selenium.Dimension;

public class TestContext extends PageComponentContext {
	private static final ThreadLocal<TestProperties> props = ThreadLocal.withInitial(TestProperties::new);

	public static TestProperties getTestProperties() {
		return props.get();
	}

	public static void init(String contextName) {
		initContext(contextName, () -> getTestProperties().getDriverProvider().getNewDriverInstance());
		String res = getTestProperties().getScreenSize();
		if (res != null) {
			String[] resWH = res.toLowerCase().split("x");
			int width = Integer.parseInt(resWH[0].trim());
			int height = Integer.parseInt(resWH[1].trim());
			Dimension dim = new Dimension(width, height);
			getContext().getDriver().manage().window().setSize(dim);
		}
		if (getTestProperties().getElementTimeout() > 0) {
			getContext().setElementLoadTimeout(getTestProperties().getElementTimeout());
		}
		if (getTestProperties().getPageTimeout() > 0) {
			getContext().setPageLoadTimeOut(getTestProperties().getPageTimeout() * 1000);
		}
	}

}