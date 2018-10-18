package ui.auto.core.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class WebDriverInstaller {

    public void installDriver(String driverName, String driverPropertyName) {
        String os = getOS();
        String subFolder = "/webdrivers/" + getOS() + "/";
        if (os.equals("windows")) {
            driverName = driverName +".exe";
        }
        String driverPath = System.getProperty("user.home") + subFolder + driverName;
        try {
            File fileTarget = new File(driverPath);
            File fileSource = File.createTempFile("driverName", "");
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream(subFolder + driverName), fileSource);
            if (!FileUtils.contentEquals(fileSource, fileTarget)) {
                FileUtils.copyFile(fileSource, fileTarget);
                fileTarget.setExecutable(true, true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.setProperty(driverPropertyName, driverPath);
    }

    private String getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.startsWith("mac")) {
            return "mac";
        }
        if (os.startsWith("linux")) {
            return "linux";
        }
        if (os.startsWith("windows")) {
            return "windows";
        }
        return "unknown";
    }

}
