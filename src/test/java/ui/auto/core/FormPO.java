package ui.auto.core;

import ui.auto.core.components.WebComponent;
import ui.auto.core.support.PageObjectModel;

public class FormPO extends PageObjectModel {
    private WebComponent name;
    private WebComponent age;

    @Override
    public String toString() {
        return "FormPO{" +
                "name=" + name.getData() +
                ", age=" + age.getData() +
                '}';
    }
}
