/*
Copyright 2010-2024 Michael Braiman braimanm@gmail.com
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.braimanm.uitaf.testng;

import com.braimanm.uitaf.support.TestContext;
import io.qameta.allure.*;
import io.qameta.allure.model.Link;
import io.qameta.allure.model.*;
import io.qameta.allure.testng.TestInstanceParameter;
import io.qameta.allure.testng.config.AllureTestNgConfig;
import io.qameta.allure.util.AnnotationUtils;
import io.qameta.allure.util.ObjectUtils;
import io.qameta.allure.util.ResultsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.*;
import org.testng.annotations.Parameters;
import org.testng.internal.ConstructorOrMethod;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.util.ResultsUtils.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;

@SuppressWarnings("unused")
public class AllureTestNGListener implements
		ISuiteListener,
		ITestListener,
		IInvokedMethodListener,
		IConfigurationListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(AllureTestNGListener.class);
	private static final String ALLURE_UUID = "ALLURE_UUID";
	private static final List<Class<?>> INJECTED_TYPES = Arrays.asList(
			ITestContext.class, ITestResult.class, XmlTest.class, Method.class, Object[].class
	);

	private static String safeExtractSuiteName(final ITestClass testClass) {
		final Optional<XmlTest> xmlTest = Optional.ofNullable(testClass.getXmlTest());
		return xmlTest.map(XmlTest::getSuite).map(XmlSuite::getName).orElse("Undefined suite");
	}

	private static String safeExtractTestTag(final ITestClass testClass) {
		final Optional<XmlTest> xmlTest = Optional.ofNullable(testClass.getXmlTest());
		return xmlTest.map(XmlTest::getName).orElse("Undefined testng tag");
	}

	private static String safeExtractTestClassName(final ITestClass testClass) {
		return firstNonEmpty(testClass.getTestName(), testClass.getName()).orElse("Undefined class name");
	}

	private final AllureLifecycle lifecycle;
	private final AllureTestNgConfig config;

	AllureTestNGListener(final AllureLifecycle lifecycle, final AllureTestNgConfig config) {
		this.lifecycle = lifecycle;
		this.config = config;
	}

	public AllureTestNGListener(final AllureLifecycle lifecycle) {
		this(lifecycle, AllureTestNgConfig.loadConfigProperties());
	}

	public AllureTestNGListener() {
		this(Allure.getLifecycle());
	}

	public AllureLifecycle getLifecycle() {
		return lifecycle;
	}

	@Override
	public void onStart(final ISuite suite) {
		Integer threadCount = TestContext.getTestProperties().getThreadCount();
		if (threadCount != null) {
			suite.getXmlSuite().setThreadCount(threadCount);
		}
		final TestResultContainer container = new TestResultContainer();
		container.setUuid(getUniqueUuid(suite));
		container.setName(suite.getName());
		container.setStart(System.currentTimeMillis());
		getLifecycle().startTestContainer(container);
	}

	@Override
	public void onFinish(final ISuite suite) {
		final String uuid = getUniqueUuid(suite);
		getLifecycle().stopTestContainer(uuid);
		getLifecycle().writeTestContainer(uuid);
	}


	@Override
	public void onStart(final ITestContext iTestContext) {
		final String parentUuid = getUniqueUuid(iTestContext.getSuite());
		final TestResultContainer container = new TestResultContainer();
		container.setUuid(getUniqueUuid(iTestContext));
		container.setName(iTestContext.getName());
		container.setStart(System.currentTimeMillis());
		getLifecycle().startTestContainer(parentUuid, container);

		if (!config.isHideDisabledTests()) {
			iTestContext.getExcludedMethods().stream()
					.filter(ITestNGMethod::isTest)
					.filter(method -> !method.getEnabled())
					.forEach(method -> createFakeResult(iTestContext, method));
		}

		for (ITestNGMethod method : iTestContext.getAllTestMethods()) {
			Retry retry = method.getConstructorOrMethod().getMethod().getAnnotation(Retry.class);
			if (method.isTest()  && retry != null) {
				method.setRetryAnalyzerClass(RetryListener.class);
			}
		}

	}

	protected void createFakeResult(final ITestContext context, final ITestNGMethod method) {
		final String uuid = UUID.randomUUID().toString();
		final String parentUuid = UUID.randomUUID().toString();
		startTestCase(context, method, method.getTestClass(), new Object[]{}, parentUuid, uuid);
		stopTestCase(uuid, null, null);
	}

	@Override
	public void onFinish(final ITestContext context) {
		final String uuid = getUniqueUuid(context);
		getLifecycle().stopTestContainer(uuid);
		getLifecycle().writeTestContainer(uuid);
	}


	@Override
	public void onTestStart(final ITestResult testResult) {
		final String parentUuid = getUniqueUuid(testResult.getTestContext());
		final String uuid = getUniqueUuid(testResult);
		startTestCase(testResult, parentUuid, uuid);
	}

	protected void startTestCase(final ITestResult testResult,
								 final String parentUuid,
								 final String uuid) {
		startTestCase(
				testResult.getTestContext(),
				testResult.getMethod(),
				testResult.getTestClass(),
				testResult.getParameters(),
				parentUuid,
				uuid
		);
	}

	@SuppressWarnings({"Indentation", "PMD.ExcessiveMethodLength"})
	protected void startTestCase(final ITestContext context,
								 final ITestNGMethod method,
								 final IClass iClass,
								 final Object[] params,
								 final String parentUuid,
								 final String uuid) {
		final ITestClass testClass = method.getTestClass();
		final List<Label> labels = new ArrayList<>();
		labels.addAll(getProvidedLabels());
		labels.addAll(Arrays.asList(
				//Packages grouping
				createPackageLabel(testClass.getName()),
				createTestClassLabel(testClass.getName()),
				createTestMethodLabel(method.getMethodName()),

				//xUnit grouping
				createParentSuiteLabel(safeExtractSuiteName(testClass)),
				createSuiteLabel(safeExtractTestTag(testClass)),
				createSubSuiteLabel(safeExtractTestClassName(testClass)),

				//Timeline grouping
				createHostLabel(),
				createThreadLabel(),

				createFrameworkLabel("testng"),
				createLanguageLabel("java")
		));
		labels.addAll(getLabels(method, iClass));
		final List<Parameter> parameters = getParameters(context, method, params);
		final TestResult result = new TestResult()
				.setUuid(uuid)
				.setHistoryId(getHistoryId(method, parameters))
				.setName(getMethodName(method))
				.setFullName(getQualifiedName(method))
				.setStatusDetails(new StatusDetails()
						.setFlaky(isFlaky(method, iClass))
						.setMuted(isMuted(method, iClass)))
				.setParameters(parameters)
				.setLinks(getLinks(method, iClass))
				.setLabels(labels);

		processDescription(
				getClass().getClassLoader(),
				method.getConstructorOrMethod().getMethod(),
				result::setDescription,
				result::setDescriptionHtml
		);

		getLifecycle().scheduleTestCase(parentUuid, result);
		getLifecycle().startTestCase(uuid);
	}

	@Override
	public void onTestSuccess(final ITestResult testResult) {
		final String uuid = getUniqueUuid(testResult);
		getLifecycle().updateTestCase(uuid, setStatus(Status.PASSED));
		getLifecycle().stopTestCase(uuid);
		getLifecycle().writeTestCase(uuid);
	}

	@Override
	public void onTestFailure(final ITestResult testResult) {
		if (testResult.getAttribute(ALLURE_UUID) == null) {
			onTestStart(testResult);
		}
		final String uuid = getUniqueUuid(testResult);
		final Throwable throwable = testResult.getThrowable();
		final Status status = getStatus(throwable);
		TestNGBase.takeScreenshot("Screenshot");
		stopTestCase(uuid, throwable, status);
	}

	protected void stopTestCase(final String uuid, final Throwable throwable, final Status status) {
		final StatusDetails details = getStatusDetails(throwable).orElse(null);
		getLifecycle().updateTestCase(uuid, setStatus(status, details));
		getLifecycle().stopTestCase(uuid);
		getLifecycle().writeTestCase(uuid);
	}

	@Override
	public void onTestSkipped(final ITestResult testResult) {
		if (testResult.getAttribute(ALLURE_UUID) == null) {
			onTestStart(testResult);
		}
		final String uuid = getUniqueUuid(testResult);
		if (testResult.getMethod().getRetryAnalyzerClass() != null) {
			TestNGBase.takeScreenshot("Screenshot");
		}
		stopTestCase(uuid, testResult.getThrowable(), Status.SKIPPED);
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(final ITestResult result) {
		//do nothing
	}

	@Override
	public void beforeInvocation(final IInvokedMethod method, final ITestResult testResult) {
		if (method.isConfigurationMethod()) {
			String uuid = getUniqueUuid(testResult);
			String parentUuid = getUniqueUuid(testResult.getTestContext());
			if (isBeforeFixture(method.getTestMethod())) {
				getLifecycle().startPrepareFixture(parentUuid, uuid, getFixtureResult(method.getTestMethod()));
			}
			if (isAfterFixture(method.getTestMethod())) {
				getLifecycle().startTearDownFixture(parentUuid, uuid, getFixtureResult(method.getTestMethod()));
			}
		}
	}

	boolean isBeforeFixture(ITestNGMethod method) {
		return method.isBeforeSuiteConfiguration() || method.isBeforeTestConfiguration()
				|| method.isBeforeClassConfiguration() || method.isBeforeGroupsConfiguration()
				|| method.isBeforeMethodConfiguration();
	}

	boolean isAfterFixture(ITestNGMethod method) {
		return method.isAfterSuiteConfiguration() || method.isAfterTestConfiguration()
				|| method.isAfterClassConfiguration() || method.isAfterGroupsConfiguration()
				|| method.isAfterMethodConfiguration();
	}


	private String getQualifiedName(final ITestNGMethod method) {
		return method.getRealClass().getName() + "." + method.getMethodName();
	}

	private FixtureResult getFixtureResult(final ITestNGMethod method) {
		final FixtureResult fixtureResult = new FixtureResult()
				.setName(getMethodName(method))
				.setStart(System.currentTimeMillis())
				.setDescription(method.getDescription())
				.setStage(Stage.RUNNING);

		processDescription(
				getClass().getClassLoader(),
				method.getConstructorOrMethod().getMethod(),
				fixtureResult::setDescription,
				fixtureResult::setDescriptionHtml
		);
		return fixtureResult;
	}

	@Override
	public void afterInvocation(final IInvokedMethod method, final ITestResult testResult) {
		//do nothing
	}

	@Override
	public void onConfigurationSuccess(final ITestResult itr) {
		String uuid = getUniqueUuid(itr);
		lifecycle.updateFixture(uuid, fixtureResult -> {
			fixtureResult.setStage(Stage.FINISHED);
			fixtureResult.setStatus(Status.PASSED);
		});
		lifecycle.stopFixture(getUniqueUuid(itr));
	}

	@Override
	public void onConfigurationFailure(final ITestResult itr) {
		String uuid = getUniqueUuid(itr);
		final StatusDetails details = getStatusDetails(itr.getThrowable()).orElse(null);
		lifecycle.updateFixture(uuid, fixtureResult -> {
			fixtureResult.setStage(Stage.FINISHED);
			fixtureResult.setStatus(Status.BROKEN);
			fixtureResult.setStatusDetails(details);
		});
		lifecycle.stopFixture(getUniqueUuid(itr));
	}

	@Override
	public void onConfigurationSkip(final ITestResult itr) {
		if (itr.getAttribute(ALLURE_UUID) == null) {
			String uuid = getUniqueUuid(itr);
			String parentUuid = getUniqueUuid(itr.getTestContext());
			if (isBeforeFixture(itr.getMethod())) {
				getLifecycle().startPrepareFixture(parentUuid, uuid, getFixtureResult(itr.getMethod()));
			}
			if (isAfterFixture(itr.getMethod())) {
				getLifecycle().startTearDownFixture(parentUuid, uuid, getFixtureResult(itr.getMethod()));
			}
		}
		String uuid = getUniqueUuid(itr);
		lifecycle.updateFixture(uuid, fixtureResult -> {
			fixtureResult.setStage(Stage.FINISHED);
			fixtureResult.setStatus(Status.SKIPPED);
		});
		lifecycle.stopFixture(getUniqueUuid(itr));
	}

	protected String getHistoryId(final ITestNGMethod method, final List<Parameter> parameters) {
		final MessageDigest digest = getMd5Digest();
		final String testClassName = method.getTestClass().getName();
		final String methodName = method.getMethodName();
		digest.update(testClassName.getBytes(UTF_8));
		digest.update(methodName.getBytes(UTF_8));
		parameters.stream()
				.sorted(comparing(Parameter::getName).thenComparing(Parameter::getValue))
				.forEachOrdered(parameter -> {
					digest.update(parameter.getName().getBytes(UTF_8));
					digest.update(parameter.getValue().getBytes(UTF_8));
				});
		final byte[] bytes = digest.digest();
		return bytesToHex(bytes);
	}

	protected Status getStatus(final Throwable throwable) {
		return ResultsUtils.getStatus(throwable).orElse(Status.BROKEN);
	}

	private List<Label> getLabels(final ITestNGMethod method, final IClass iClass) {
		final List<Label> labels = new ArrayList<>();
		getMethod(method)
				.map(AnnotationUtils::getLabels)
				.ifPresent(labels::addAll);
		getClass(iClass)
				.map(AnnotationUtils::getLabels)
				.ifPresent(labels::addAll);

		getMethod(method)
				.map(this::getSeverity)
				.filter(Optional::isPresent)
				.orElse(getClass(iClass).flatMap(this::getSeverity))
				.map(ResultsUtils::createSeverityLabel)
				.ifPresent(labels::add);
		return labels;
	}

	private Optional<SeverityLevel> getSeverity(final AnnotatedElement annotatedElement) {
		return Stream.of(annotatedElement.getAnnotationsByType(Severity.class))
				.map(Severity::value)
				.findAny();
	}

	private List<Link> getLinks(final ITestNGMethod method, final IClass iClass) {
		final List<Link> links = new ArrayList<>();
		getMethod(method)
				.map(AnnotationUtils::getLinks)
				.ifPresent(links::addAll);
		getClass(iClass)
				.map(AnnotationUtils::getLinks)
				.ifPresent(links::addAll);
		return links;
	}

	private boolean isFlaky(final ITestNGMethod method, final IClass iClass) {
		final boolean flakyMethod = getMethod(method)
				.map(m -> m.isAnnotationPresent(Flaky.class))
				.orElse(false);
		final boolean flakyClass = getClass(iClass)
				.map(clazz -> clazz.isAnnotationPresent(Flaky.class))
				.orElse(false);
		return flakyMethod || flakyClass;
	}

	private boolean isMuted(final ITestNGMethod method, final IClass iClass) {
		final boolean mutedMethod = getMethod(method)
				.map(m -> m.isAnnotationPresent(Muted.class))
				.orElse(false);
		final boolean mutedClass = getClass(iClass)
				.map(clazz -> clazz.isAnnotationPresent(Muted.class))
				.orElse(false);
		return mutedMethod || mutedClass;
	}

	private Optional<Method> getMethod(final ITestNGMethod method) {
		return Optional.ofNullable(method)
				.map(ITestNGMethod::getConstructorOrMethod)
				.map(ConstructorOrMethod::getMethod);
	}

	private Optional<Class<?>> getClass(final IClass iClass) {
		return Optional.ofNullable(iClass)
				.map(IClass::getRealClass);
	}

	private String getUniqueUuid(final IAttributes suite) {
		if (Objects.isNull(suite.getAttribute(ALLURE_UUID))) {
			suite.setAttribute(ALLURE_UUID, UUID.randomUUID().toString());
		}
		return Objects.toString(suite.getAttribute(ALLURE_UUID));
	}

	private List<Parameter> getParameters(final ITestContext context,
										  final ITestNGMethod method,
										  final Object... parameters) {
		final Map<String, String> result = new HashMap<>(
				context.getCurrentXmlTest().getAllParameters()
		);
		final Object instance = method.getInstance();
		if (nonNull(instance)) {
			Stream.of(instance.getClass().getDeclaredFields())
					.filter(field -> field.isAnnotationPresent(TestInstanceParameter.class))
					.forEach(field -> {
						final String name = Optional.ofNullable(field.getAnnotation(TestInstanceParameter.class))
								.map(TestInstanceParameter::value)
								.filter(s -> !s.isEmpty())
								.orElseGet(field::getName);
						try {
							field.setAccessible(true);
							final String value = ObjectUtils.toString(field.get(instance));
							result.put(name, value);
						} catch (IllegalAccessException e) {
							LOGGER.debug("Could not access field value");
						}
					});
		}

		getMethod(method).ifPresent(m -> {
			final Class<?>[] parameterTypes = m.getParameterTypes();

			if (parameterTypes.length != parameters.length) {
				return;
			}

			final String[] providedNames = Optional.ofNullable(m.getAnnotation(Parameters.class))
					.map(Parameters::value)
					.orElse(new String[]{});

			final String[] reflectionNames = Stream.of(m.getParameters())
					.map(java.lang.reflect.Parameter::getName)
					.toArray(String[]::new);

			int skippedCount = 0;
			for (int i = 0; i < parameterTypes.length; i++) {
				final Class<?> parameterType = parameterTypes[i];
				if (INJECTED_TYPES.contains(parameterType)) {
					skippedCount++;
					continue;
				}

				final int indexFromAnnotation = i - skippedCount;
				if (indexFromAnnotation < providedNames.length) {
					result.put(providedNames[indexFromAnnotation], ObjectUtils.toString(parameters[i]));
					continue;
				}

				if (i < reflectionNames.length) {
					result.put(reflectionNames[i], ObjectUtils.toString(parameters[i]));
				}
			}

		});

		return result.entrySet().stream()
				.map(entry -> createParameter(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
	}

	private String getMethodName(final ITestNGMethod method) {
		return firstNonEmpty(
				method.getDescription(),
				method.getMethodName(),
				getQualifiedName(method)).orElse("Unknown");
	}

	@SuppressWarnings("SameParameterValue")
	private Consumer<TestResult> setStatus(final Status status) {
		return result -> result.setStatus(status);
	}

	private Consumer<TestResult> setStatus(final Status status, final StatusDetails details) {
		return result -> {
			result.setStatus(status);
			if (nonNull(details)) {
				result.getStatusDetails().setTrace(details.getTrace());
				result.getStatusDetails().setMessage(details.getMessage());
			}
		};
	}


}
