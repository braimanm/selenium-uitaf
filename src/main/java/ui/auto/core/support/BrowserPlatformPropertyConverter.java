package ui.auto.core.support;

import org.apache.commons.beanutils.Converter;
import org.openqa.selenium.Platform;

public class BrowserPlatformPropertyConverter implements Converter {
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object convert(Class clz, Object obj) {
		if (!(obj instanceof String)){
			return null;
		}

        return Platform.valueOf((String) obj);
    }
	

}
