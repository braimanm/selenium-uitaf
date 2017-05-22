package ui.auto.core.support;


import ru.qatools.properties.converters.Converter;

public class BrowserTypePropertyConverter implements Converter {


	@Override
	public Object convert(String from) throws Exception {
		return WebDriverTypeEnum.valueOf(from);
	}
}
