package com.braimanm.uitaf.support;

import com.braimanm.ui.auto.context.PageComponentContext;
import org.openqa.selenium.Dimension;

public class TestContext extends PageComponentContext {
	private static final UITAFProperties frameworkProps = new UITAFProperties();
	private static final ThreadLocal<TestProperties> props = ThreadLocal.withInitial(TestContext::loadTestProperties);
	private static final DriverProvider driverProvider = loadDriverProvider();

	private static TestProperties loadTestProperties() {
		String className = frameworkProps.getTestPropertiesClass();
		if (className != null && !className.trim().isEmpty()) {
			try {
				Class<?> clazz = Class.forName(className.trim());
				if (TestProperties.class.isAssignableFrom(clazz)) {
					return (TestProperties) clazz.getDeclaredConstructor().newInstance();
				} else {
					throw new RuntimeException("Class " + className + " does not extend TestProperties");
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to instantiate test.properties class: " + className, e);
			}
		}
		return new TestProperties();
	}

	private static DriverProvider loadDriverProvider() {
		String className = frameworkProps.getDriverProviderClass();
		if (className == null || className.trim().isEmpty()) {
			throw new RuntimeException("Missing required property: driver.provider in uitaf.properties");
		}
		try {
			Class<?> clazz = Class.forName(className.trim());
			if (DriverProvider.class.isAssignableFrom(clazz)) {
				return (DriverProvider) clazz.getDeclaredConstructor().newInstance();
			} else {
				throw new RuntimeException("Class " + className + " does not implement DriverProvider");
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to instantiate driver.provider class: " + className, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends TestProperties> T getTestProperties() {
		return (T) props.get();
	}

	public static void init(String contextName) {
		initContext(contextName, () ->
				driverProvider.getNewDriverInstance(getTestProperties()));
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