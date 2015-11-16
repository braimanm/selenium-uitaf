package ui.auto.core.support;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.testng.TestNG;
import org.testng.reporters.Files;

import com.thoughtworks.xstream.XStream;

import ru.yandex.qatools.allure.AllureMain;
import ru.yandex.qatools.allure.config.AllureConfig;
import ru.yandex.qatools.commons.model.Environment;
import ui.auto.core.testng.AllureTestNGListener;


public class TestRunner {
	private String resultsFolder;
	private String reportFolder;
	private int port = 8090;
	
	public TestRunner(){
		TestProperties props = TestProperties.getInstance();
		AllureConfig config=new AllureConfig();
		resultsFolder = config.getResultsDirectory().getAbsolutePath();
		reportFolder = props.getReportFolder().getAbsolutePath();
	}
	
	public int runTests(List<String> suites) throws IOException{
		for (String suite:suites){
			File file = new File(suite);
			InputStream is=Thread.currentThread().getContextClassLoader().getResourceAsStream(suite);
			file=new File(suite);
			FileUtils.copyInputStreamToFile(is,file);
		}
		Object atl=new AllureTestNGListener();
		TestNG testNg=new TestNG();
		testNg.addListener(atl);
		testNg.setTestSuites(suites);
		testNg.setSuiteThreadPoolSize(5);
		testNg.run();
		saveEnvironment();
		return testNg.getStatus();
	}
	
	public void generateReport() throws IOException {
		String[] arguments = {resultsFolder,reportFolder};
		AllureMain.main(arguments);
	}
	
	public void openReport() throws Exception {
		Server server = setUpServer();
		server.start();
		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().browse(server.getURI());
		}
        server.join();
	}
	
	 private Server setUpServer() {
		 	String p = System.getProperty("local.server.port");
		 	if (p != null) {
		 		port = Integer.parseInt(p);
		 	}
	        Server server = new Server(port);
	        ResourceHandler handler = new ResourceHandler();
	        handler.setDirectoriesListed(true);
	        handler.setWelcomeFiles(new String[]{"index.html"});
	        handler.setResourceBase(reportFolder);
	        HandlerList handlers = new HandlerList();
	        handlers.setHandlers(new Handler[]{handler, new DefaultHandler()});
	        server.setStopAtShutdown(true);
	        server.setHandler(handlers);
	        server.setStopAtShutdown(true);
	        return server;
	    }
	
		public void saveEnvironment(){
			TestProperties prop=TestProperties.getInstance();
			Environment environment=new Environment().withName("Environment");
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
}

