package ui.auto.core.support;

import ru.qatools.properties.converters.Converter;

public class EnvironmentPropertyConverter implements Converter<EnvironmentsSetup.Environment> {

    @Override
    public EnvironmentsSetup.Environment convert(String from) {
        if (from.trim().isEmpty()) return null;
        String[] envConfig = from.trim().split(":");
        if (envConfig.length < 2) throw new RuntimeException("Please provide test.env property in the following format <config file path>:<environment name>");
        String config = envConfig[0].trim();
        String env = envConfig[1].trim();
        EnvironmentsSetup envSetup = new EnvironmentsSetup().fromResource(config);
        return envSetup.getEnvironment(env);
    }


}
