# selenium-uitaf
## Minimalistic selenium based UI testing framework with page-component open source project and Allure reporting.

* UITAF (UI Test Automation Framework) is another attempt to make life of automation developers easier by providing concise test automation development approach with automatic generation of test data-sets from page-objects and generation of detailed report at the end of the test execution. 

* The UITAF uses new approach for business oriented testing by introducing new design pattern called “domain-objects” where multiple web pages are involved across single business scenario.  

* UITAF supports concurrency right out of the box by invoking multiple test cases using multiple supported browsers on the single machine, on the selenium grid or on Virtual Machines in the cloud.  

* UITAF seamlessly integrates with continuous integration systems allowing easy configuration for running the tests jobs against multiple environments, it also has capability to embed test report with any job run by hosting the report on the CI server.  

## The following Diagram shows the architecture of the UITAF: 

![image](https://user-images.githubusercontent.com/7651167/61151451-f7806980-a4b3-11e9-8e34-002d7c7458ad.png)

*	**Selenium WebDriver** API drives the Web Browser to simulate user actions on the Web Application under the Test.
*	**Web Page Components** comprise library of reusable components which encapsulate knowledge of how to operate and test underlying HTML composite component. Automation developer can declare and use those components in Page Objects without dealing with complexity of underlying UI technology. Page Components are highly reusable and mostly developed using Selenium WebDriver API.
*	**Page Objects Models** represents components and services offered by particular web page. Page Objects aggregates and operates various Web Page Components to provide Web Page HTML oriented methods. Page Object Models are highly reusable and doesn’t uses Selenium WebDriver API
*	**Domain Object Models** are the next layer above Page Objects Models which encapsulates business logic behaviors. Domain Object Models aggregates and operates Page Objects to provide methods which are business logic oriented and where the business logic spans across multiple Web Pages.
*	**Test Orchestration and Concurrency** is used for invocation of multiple test scenarios, data provisioning and concurrency. This sub-system allocates one execution thread per test from its configurable pull of threads.
*	**XML Test Data Sets** are XML structured files which describes the input parameters for the test and expected results. During the test, input data is used to feed application with specific data and expected results are used for validating results of specific business operations.
*	**Test Scripts** are code written in Java language. Test scripts are following use case scenarios to validate various expected behaviors and they can use combination of Domain Objects and Page Objects for the test development purposes.
*	**Test Context + Test Parameters** responsible for setup of global configurable environment including specific for the test WebDriver instance and various test related timeouts.
*	**Test Lifecycle** aggregates multiple events across the framework subsystems which are related to the test execution and uses them in report generation. The following are examples of various events that are recorded by the Test Lifecycle: suite start, suite end, test start, test end, step start, step end, create attachment and others. During the event collection Test Lifecycle writes all the events in to the file system usually represented by directory named “results” under the “target” folder.
*	**Test Report** is generated from Test Lifecycle artifacts and represents Report Web Application. User can use web browser to load the report and navigate through various test execution specific data views.
