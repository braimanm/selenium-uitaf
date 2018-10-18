package ui.auto.core.support;


import ru.qatools.properties.converters.Converter;

public class BrowserTypePropertyConverter implements Converter<WebDriverTypeEnum> {

	@Override
	public WebDriverTypeEnum convert(String from) {
		return WebDriverTypeEnum.valueOf(from);
	}
}
