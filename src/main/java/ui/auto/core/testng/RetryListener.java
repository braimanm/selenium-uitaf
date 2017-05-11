package ui.auto.core.testng;


import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryListener implements IRetryAnalyzer {
    private int count = 0;
    private int max;

    public RetryListener(int times) {
        max = times;
    }

    @Override
    public boolean retry(ITestResult iTestResult) {
        if (count < max) {
            count++;
            return true;
        }
        return false;
    }
}
