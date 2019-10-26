/*
Copyright 2010-2019 Michael Braiman braimanm@gmail.com
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

package ui.auto.core.support;

import datainstiller.data.DataAliases;
import datainstiller.data.DataPersistence;
import org.apache.commons.jexl3.JexlContext;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.events.MakeAttachmentEvent;
import ru.yandex.qatools.allure.events.TestCaseEvent;
import ru.yandex.qatools.allure.model.Description;
import ru.yandex.qatools.allure.model.DescriptionType;
import ru.yandex.qatools.allure.model.Label;
import ru.yandex.qatools.allure.model.LabelName;
import ui.auto.core.context.PageComponentContext;
import ui.auto.core.pagecomponent.PageObject;
import ui.auto.core.pagecomponent.SkipAutoFill;
import ui.auto.core.testng.TestNGBase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageObjectModel extends PageObject {

    private void resolveGlobalAliasses() {
        DataAliases global = PageComponentContext.getGlobalAliases();
        for (String aliasKey : global.keySet()) {
            String aliasValue = global.get(aliasKey);
            if (aliasValue != null && aliasValue.matches(".*\\$\\{[\\w-]+}.*")) {
                Pattern pat = Pattern.compile("\\$\\{[\\w-]+}");
                Matcher mat = pat.matcher(aliasValue);
                while (mat.find()) {
                    String alias = mat.group();
                    String key = alias.replace("${", "").replace("}", "");
                    if (global.containsKey(key)) {
                        String value = global.get(key);
                        if (value != null) {
                            aliasValue = aliasValue.replace(alias, value);
                        }
                    }
                }
                global.put(aliasKey, aliasValue);
            }
        }
    }

    @Override
    protected void initJexlContext(JexlContext jexlContext) {
        LocalDateTime now = LocalDateTime.now();
        jexlContext.set("year_month_day", DateTimeFormatter.ofPattern("yyyy-M-d").format(now));
        jexlContext.set("day_month_year", DateTimeFormatter.ofPattern("d-M-yyyy").format(now));
        jexlContext.set("month_day_year", DateTimeFormatter.ofPattern("M-d-yyyy").format(now));
        jexlContext.set("time_stamp", DateTimeFormatter.ofPattern("yyyyMMddkkmmss-S").format(now));
        String timeStampThreadId = DateTimeFormatter.ofPattern("yyMMddkkmmss").format(now) + Thread.currentThread().getId();
        jexlContext.set("time_stamp_thread", timeStampThreadId);

        EnvironmentsSetup.Environment env = TestContext.getTestProperties().getTestEnvironment();
        if (env != null) {
            jexlContext.set("env", env);
            jexlContext.set("userProvider", UserProvider.getInstance());
        }
    }

    private List<Label> getLabels(DataPersistence data, String labelAlias, LabelName labelName) {
        DataAliases aliases = data.getDataAliases();
        String label = aliases.get(labelAlias);
        List<Label> labels = new ArrayList<>();
        if (label != null) {
            for (String value : label.split(",")) {
                labels.add(new Label().withName(labelName.value()).withValue(value.trim()));
            }
        }
        return labels;
    }

    private void overwriteTestParameters(DataPersistence data) {
        DataAliases aliases = data.getDataAliases();
        if (aliases == null) return;
        String name = aliases.get("test-name");
        String description = aliases.get("test-description");

        List<Label> labels = new ArrayList<>();
        labels.addAll(getLabels(data, "test-features", LabelName.FEATURE));
        labels.addAll(getLabels(data, "test-stories", LabelName.STORY));
        labels.addAll(getLabels(data, "test-issues", LabelName.ISSUE));
        labels.addAll(getLabels(data, "test-IDs", LabelName.TEST_ID));
        labels.addAll(getLabels(data, "test-severity", LabelName.SEVERITY));

        Allure.LIFECYCLE.fire((TestCaseEvent) context -> {
            context.getLabels().addAll(labels);
            if (name != null) {
                context.setName(name);
            }
            if (description != null) {
                context.setDescription(new Description().withType(DescriptionType.MARKDOWN).withValue(description));
            }
        });
    }

    @Override
    public <T extends DataPersistence> T fromResource(String resourceFilePath, boolean resolveAliases) {
        T data = super.fromResource(resourceFilePath, resolveAliases);
        resolveGlobalAliasses();
        overwriteTestParameters(data);
        TestNGBase.attachDataSet(data, resourceFilePath);

        String aliases = getXstream().toXML(TestContext.getGlobalAliases());
        MakeAttachmentEvent ev = new MakeAttachmentEvent(aliases.getBytes(), "Global Aliases : " + resourceFilePath, "text/xml");
        Allure.LIFECYCLE.fire(ev);
        return data;
    }

    @Override
    protected void autoFillPage(boolean validate) {
        if (context == null) throw new RuntimeException("PageObject is not initialized, invoke initPage method!");
        enumerateFields((pageComponent, field) -> {
            if (!field.isAnnotationPresent(SkipAutoFill.class))
                setElementValue(pageComponent, validate);
                if (pageComponent != null && pageComponent.getData() != null) {
                    String fieldName = field.getName();
                    if (field.isAnnotationPresent(FieldName.class)) {
                        fieldName = field.getAnnotation(FieldName.class).value();
                    }
                    reportForAutoFill(fieldName, pageComponent.getData());
                }
        });
    }

    protected void reportForAutoFill(String fieldName, String value) {}

}