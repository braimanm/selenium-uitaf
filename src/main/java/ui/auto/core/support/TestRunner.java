package ui.auto.core.support;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.FileUtils;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.reporters.Files;
import ru.yandex.qatools.allure.AllureMain;
import ru.yandex.qatools.allure.config.AllureConfig;
import ru.yandex.qatools.commons.model.Environment;
import ui.auto.core.testng.TestParameterValidator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class TestRunner {
	private String resultsFolder;
	private String reportFolder;

	public TestRunner(){
		TestProperties props = TestContext.getTestProperties();
		System.setProperty("allure.results.directory", props.getResultsFolder().getAbsolutePath());
		System.setProperty("allure.testng.parameters.enabled", "false");
		AllureConfig config=new AllureConfig();
		resultsFolder = config.getResultsDirectory().getAbsolutePath();
		reportFolder = props.getReportFolder().getAbsolutePath();
		props.setReportUrlPatterns();

	}

	public int runTests(List<String> suites) throws IOException{
		if (suites.isEmpty()) {
			throw new RuntimeException("Suite file names where not provided! \nPlease provide suite file names Ex: -Dtest.suites=<SUITE FILE PATH>");
		}
		for (String suite:suites){
			InputStream is=Thread.currentThread().getContextClassLoader().getResourceAsStream(suite);
			File file = new File(suite);
			if (is != null) {
				FileUtils.copyInputStreamToFile(is, file);
			} else if (!file.exists()) {
				throw new RuntimeException("Suite file '" + suite + "' does not exists on the file system!");
			}
		}
		TestNG testNg = new TestNG(false);
		testNg.addListener((ITestNGListener) new TestParameterValidator());
		testNg.setTestSuites(suites);
		testNg.setSuiteThreadPoolSize(1);
		testNg.run();
		saveEnvironment();
		return testNg.getStatus();
	}

	public void generateReport() throws IOException {
		String[] arguments = {resultsFolder, reportFolder};
		AllureMain.main(arguments);
		brandingMod();
	}

	private void brandingMod() throws IOException {
		String logo = "/61aae920ab2f8fe604ba57b135aa9919.png";
		String index = "/index.html";
		InputStream logoStr = Thread.currentThread().getContextClassLoader().getResourceAsStream("rep" + logo);
		InputStream indexStr = Thread.currentThread().getContextClassLoader().getResourceAsStream("rep" + index);
		Files.copyFile(logoStr, new File(reportFolder + logo));
		Files.copyFile(indexStr, new File(reportFolder + index));
		File report = new File(reportFolder + "/app.js");
		String content = Files.readFile(report);
		content = content.replace("Allure", "<br/><br/><br/>Test Report");
		content = content.replace("Latest", TestContext.getTestProperties().getReportVersion());
		Files.writeFile(content, report );
	}

	private void saveEnvironment() {
		TestProperties prop= TestContext.getTestProperties();
		Environment environment = new Environment().withName("Environment");
		environment.withParameter(prop.getAsParameters());
		XStream xstream=new XStream();
		xstream.addImplicitArray(Environment.class,"parameter", "parameter");
		String xml=xstream.toXML(environment);
		xml=xml.replace("<ru.yandex.qatools.commons.model.Environment>","<qa:environment xmlns:qa=\"urn:model.commons.qatools.yandex.ru\">");
		xml=xml.replace("</ru.yandex.qatools.commons.model.Environment>","</qa:environment>");
		xml=xml.replace(" class=\"ru.yandex.qatools.commons.model.Parameter\"","");
		File file=new File(resultsFolder + "/environment.xml");
		try {
			Files.writeFile(xml, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteResultsFolder() throws IOException {
		File resFolder = new File(resultsFolder);
		if (resFolder.exists()) {
			FileUtils.forceDelete(resFolder);
		}
	}

	public void deleteReportFolder() throws IOException {
		File resFolder = new File(reportFolder);
		if (resFolder.exists()) {
			FileUtils.forceDelete(resFolder);
		}
	}

}