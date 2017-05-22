package ui.auto.core.support;

import org.openqa.selenium.Platform;
import ru.qatools.properties.converters.Converter;

public class BrowserPlatformPropertyConverter implements Converter {

	@Override
	public Object convert(String from) throws Exception {
		return Platform.valueOf(from);
	}
}
