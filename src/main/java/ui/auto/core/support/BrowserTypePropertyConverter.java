package ui.auto.core.support;

import org.apache.commons.beanutils.Converter;

public class BrowserTypePropertyConverter implements Converter {

	@SuppressWarnings("rawtypes")
	@Override
	public Object convert(Class clz, Object obj) {
		if (!(obj instanceof String)){
			return null;
		}

		return WebDriverTypeEnum.valueOf((String) obj);
	}

}
