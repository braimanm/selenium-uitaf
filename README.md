# selenium-uitaf
## Minimalistic selenium based UI testing framework with page-component open source project and Allure reporting.

* UITAF (UI Test Automation Framework) is a tool or framework that is designed to make it easier for developers to create automated tests for user interfaces (UI). It provides a concise approach for developing automated tests, and it includes features such as the automatic generation of test data sets from page objects and the generation of a detailed report at the end of the test execution. The goal of UITAF is to make it easier and more efficient for developers to create and run automated tests for UI. 

* UITAF uses a new approach for business-oriented testing by introducing a design pattern called "domain-objects". This design pattern is intended to make it easier and more efficient to create automated tests for business processes where multiple web pages are involved across a single business scenario.

* UITAF is designed to support concurrent testing. This means that it allows multiple test cases to be run at the same time using multiple supported browsers on a single machine, on a Selenium grid, or on virtual machines in the cloud. By providing this capability, UITAF aims to make it easier and more efficient for developers to perform concurrent testing of their UI. 

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


# Test Automation Components (Page Components)

UITAF is designed around the usage of Test Automation Components and I'll try to explain what they are. 
In today’s web application technologies, webpages are composed from the JavaScript UI component libraries like AngularJS, React, jQuery UI, Ext JS, JSF PrimeFaces, and others. All these libraries use special templates composed by a collection of primitive HTML elements that acts as a single UI component and JavaScript code which encapsulates the logic of those components.

![image](https://user-images.githubusercontent.com/7651167/61152108-8cd02d80-a4b5-11e9-9fe0-14a8bb024f74.png)

Here is an example of how UITAF is used: The test for the required business functionality should span across multiple pages, in our case it should use two pages Page1 which is resemble Login Page and Page2 which represents arbitrary number of UI controls for business related activity.

Unfortunately the Selenium API doesn't know about how to automate composite Web Controls or in another word "Page Components" but instead it can control primitive html elements which are scattered all around the Web Page, in our case the web controls are composite and they combines many primitive html elements and additional logic which is scripted using JavaScript library. Usually web developer is creating global library of specific functions to automate those composite web elements but those functions lives outside of the component representation and it's very hard to find which functions belongs to which component or web control.

To cope with this issue UITAF introducing new type of element which is called "Page Component" this new element is represented by a java class which encapsulates automation specific behavior which is coded once and used across many Pages which shares the same component. In our example Page1 and Page2 is composed using the same Page Components which are "Edit" and "Button" also Page2 includes other components like "Select" and "Radio Group". All those component are part of Component Library which consists of many automated components designed specifically to automate specific "JavaScript UI Framework" which is used in our Web business application under the test.

The huge benefit of creating those Page Component libraries is: that if in our quickly evolving application, developers will decided to use other JavaScript UI Framework then all we need to do is to introduce and develop new Component Library and all the other artifact of our Automation Test will remain intact. In other Test Automation Frameworks in similar case the automation developer needs to rewrite the tests from scratch.

There is another benefit of using "Page Component": currently "Page Object" design pattern can support only fields of "WebElement" interface type, but UITAF extends this capability and allows to declare specific "Page Components" as fields of the Page Object. This completely removes cluttered Selenium code from Page Objects and instead includes only Page Specific business methods without using Selenium API, in another words Selenium API is completely abstracted from the Business specific Test Automation code. In our specific example "Page Object 1" and "Page Object 2" PageObjects consists of fields which are of specific PageComponent type and they expose page oriented business methods "Login" and "Populate Form".

UITAF "Page Component" has two interfaces: one interface is facing toward technology aware functionality by providing a knowledge on how to operate and automate specific Page Components, and there is another interface facing toward DATA framework facility which abstracts our automation test from the data which is needed for Page Component population or operation. 
Data framework facility allows to invoke the same test using different data-sets and this is achieved through java object serialization & deserialization. Page Object can be serialized to XML templates, those templates can be filed with concrete data by test automation developer and deserialized during test execution without applying any data to object mapping algorithms. 

Data for Page Components can be generated with random data generators, there are many Data generators supplied by UITAF like: Human Name generator, Valid Addresses generator, Date generator, English Vocabulary word and sentence generator, numeric & alphanumeric generator, list of randomized values generator and others. 

In case of dynamic data which needs to be provided at test execution run-time, automation developer can use expression language and dynamic aliases mechanisms supported by data facilities which are built into UITAF.

"Domain Object" design pattern is a design pattern introduced and supported by UITAF, it's very similar to "Page Object" design pattern but instead of being Page oriented the "Domain Object" is strictly business oriented. Domain Object is used when you have business logic which is spanning across multiple web pages (or Page Objects). Fields of Domain Object classes are of PageObject types and they expose business methods related to business processes. In our example "Domain Object 1" Domain Object consists of two Page Objects which are needed to complete hypothetical "Submit Request" business method.

Using all aforementioned UITAF mechanisms and design patterns automation developer can write real business oriented automation test cases which are readable and understandable by any technical or business oriented stakeholder for any existing and future projects.

* #### Maven Dependency
This open source project is distributed through maven central repository. Here is a Maven dependency for UITAF project.
```
<dependency>
  <groupId>com.googlecode.page-component</groupId>
  <artifactId>ui_auto_core</artifactId>
  <version>2.5.16</version>
</dependency>
```
