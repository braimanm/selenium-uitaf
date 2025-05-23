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

package com.braimanm.uitaf.support;

import com.braimanm.datainstiller.context.DataContext;
import com.braimanm.datainstiller.data.DataAliases;
import com.braimanm.datainstiller.data.DataPersistence;
import com.braimanm.ui.auto.pagecomponent.PageObject;
import com.braimanm.ui.auto.pagecomponent.SkipAutoFill;
import com.braimanm.uitaf.testng.TestNGBase;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.Link;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.util.ResultsUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"NewClassNamingConvention", "unused"})
public class PageObjectModel extends PageObject {

    @Override
    protected void initJexlContext() {
        super.initJexlContext();
        DataAliases ga = DataContext.getGlobalAliases();
        if (!ga.containsKey("year_month_day")) {
            LocalDateTime now = LocalDateTime.now();
            ga.put("year_month_day", DateTimeFormatter.ofPattern("yyyy-M-d").format(now));
            ga.put("day_month_year", DateTimeFormatter.ofPattern("d-M-yyyy").format(now));
            ga.put("month_day_year", DateTimeFormatter.ofPattern("M-d-yyyy").format(now));
            ga.put("time_stamp", DateTimeFormatter.ofPattern("yyyyMMddkkmmss-S").format(now));
            String timeStampThreadId = DateTimeFormatter.ofPattern("yyMMddkkmmss").format(now) + Thread.currentThread().getId();
            ga.put("time_stamp_thread", timeStampThreadId);

            EnvironmentsSetup.Environment env = TestContext.getTestProperties().getTestEnvironment();
            if (env != null) {
                ga.put("env", env);
                ga.put("userProvider", UserProvider.getInstance());
            }
        }
    }

    private List<Label> getLabels(String labelNames, String labelType) {
        if (labelNames == null || labelNames.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(labelNames.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> ResultsUtils.createLabel(labelType, name))
                .collect(Collectors.toList());
    }

    private List<Link> getLinks(String linkNames, String type) {
        if (linkNames == null || linkNames.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(linkNames.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> ResultsUtils.createLink(name, null, null, type)) // Create Link objects
                .collect(Collectors.toList());
    }

    private void overwriteTestParameters(DataPersistence data) {
        DataAliases aliases = data.getDataAliases();
        if (aliases == null) return;

        String name = aliases.getAsString("test-name");
        if (name != null && !name.trim().isBlank()) {
            Allure.getLifecycle().updateTestCase(testResult -> {
                if (name.contains("+")) {
                    testResult.setName(name.replace("+",testResult.getName()));
                } else {
                    testResult.setName(name);
                }
            });
        }

        String description = aliases.getAsString("test-description");
        if (description != null && !description.trim().isBlank()) {
            Allure.description(description);
        }

        List<Label> labels = new ArrayList<>();
        labels.addAll(getLabels(aliases.getAsString("test-epic"), ResultsUtils.EPIC_LABEL_NAME));
        labels.addAll(getLabels(aliases.getAsString("test-features"), ResultsUtils.FEATURE_LABEL_NAME));
        labels.addAll(getLabels(aliases.getAsString("test-stories"), ResultsUtils.STORY_LABEL_NAME));
        labels.addAll(getLabels(aliases.getAsString("test-severity"), ResultsUtils.SEVERITY_LABEL_NAME));

        Allure.getLifecycle().updateTestCase(testResult -> testResult.getLabels().addAll(labels));

        List<Link> links = new ArrayList<>();
        links.addAll(getLinks(aliases.getAsString("test-issues"), ResultsUtils.ISSUE_LINK_TYPE));
        links.addAll(getLinks(aliases.getAsString("test-tmsLinks"), ResultsUtils.TMS_LINK_TYPE));
        links.addAll(getLinks(aliases.getAsString("test-links"), ResultsUtils.CUSTOM_LINK_TYPE));

        Allure.getLifecycle().updateTestCase(testResult -> testResult.getLinks().addAll(links));

    }

    @Override
    public <T extends DataPersistence> T fromResource(String resourceFilePath) {
        T data = super.fromResource(resourceFilePath);
        if (Allure.getLifecycle().getCurrentTestCase().isPresent()) {
            overwriteTestParameters(data);
            TestNGBase.attachDataSet(data, resourceFilePath);
        }
        return data;
    }

    @Override
    protected void autoFillPage(boolean validate) {
        if (context == null) throw new RuntimeException("PageObject is not initialized, invoke initPage method!");
        enumerateFields((pageComponent, field) -> {
            if (!field.isAnnotationPresent(SkipAutoFill.class)) {
                setElementValue(pageComponent, validate);
                if (pageComponent.getData() != null && !pageComponent.getData().isEmpty()) {
                    String fieldName;
                    if (field.isAnnotationPresent(FieldName.class)) {
                        fieldName = field.getAnnotation(FieldName.class).value();
                    } else {
                        String[] parts = StringUtils.splitByCharacterTypeCamelCase(field.getName());
                        fieldName = StringUtils.capitalize(StringUtils.join(parts, StringUtils.SPACE));
                    }
                    reportForAutoFill(fieldName, pageComponent.getData());
                }
            }
        });
    }

    /**
     * This method is used to report on each field population.
     * Simply add the @Step annotation to the overridden method.
     * @param fieldName - name of the field to be reported
     * @param value - value to be reported
     */
    protected void reportForAutoFill(String fieldName, String value) {
        hideStepParams();
    }

    /**
     * Used for hiding automatic parameters in Allure report step methods.
     */
    public void hideStepParams() {
        Allure.getLifecycle().updateStep(stepResult -> stepResult.getParameters().
                forEach(parameter -> parameter.setMode(Parameter.Mode.HIDDEN)));
    }

}