package com.braimanm.uitaf.support;

import ru.qatools.properties.Property;
import ru.qatools.properties.PropertyLoader;
import ru.qatools.properties.Resource;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "unused"})
@Resource.Classpath("uitaf.properties")
public class UITAFProperties {

	@Property("test.properties")
	private String testPropertiesClass;

	@Property("driver.provider")
	private String driverProviderClass;

	public UITAFProperties() {
		PropertyLoader.newInstance().populate(this);
	}

	public String getTestPropertiesClass() {
		return testPropertiesClass;
	}

	public String getDriverProviderClass() {
		return driverProviderClass;
	}
}