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

package ui.auto.core.support;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import ui.auto.core.context.PageComponentContext;

public class TestContext extends PageComponentContext {
	private static ThreadLocal<TestProperties> props = ThreadLocal.withInitial(TestProperties::new);

	public TestContext(WebDriver driver) {
		super(driver);
		setTimeouts();
	}

	public TestContext() {
		this(null);
	}

	public static TestProperties getTestProperties() {
		return props.get();
	}

    public void init() {
        if (driver != null) return;
        driver = getTestProperties().getDriverProvider().getNewDriverInstance();
		String res = getTestProperties().getScreenSize();
		if (res != null) {
			String[] resWH = res.toLowerCase().split("x");
			int width = Integer.parseInt(resWH[0].trim());
			int height = Integer.parseInt(resWH[1].trim());
			Dimension dim = new Dimension(width, height);
			driver.manage().window().setSize(dim);
		}
	}

	public Object getAlias(String key) {
		return getGlobalAliases().get(key);
	}

	public void setAlias(String key,String value) {
		getGlobalAliases().put(key, value);
	}

	private void setTimeouts() {
		if (getTestProperties().getElementTimeout()>0) {
			setAjaxTimeOut(getTestProperties().getElementTimeout());
		}
		if (getTestProperties().getPageTimeout()>0) {
			setWaitForUrlTimeOut(getTestProperties().getPageTimeout() * 1000);
		}
	}

}