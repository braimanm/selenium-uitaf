package ui.auto.core.support;

import ru.qatools.properties.converters.Converter;

public class DriverProviderConverter implements Converter<DriverProvider> {

    @Override
    public DriverProvider convert(String from) throws Exception {
        Class driverProvider;
        try {
            driverProvider = Class.forName(from);
        } catch( ClassNotFoundException e ) {
            throw new RuntimeException("DriverProvider " + from + " is not exist!");
        }
        return (DriverProvider) driverProvider.newInstance();
    }
}
