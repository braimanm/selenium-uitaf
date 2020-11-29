package ui.auto.core;

import org.testng.annotations.Test;
import ui.auto.core.support.TestContext;
import ui.auto.core.testng.TestNGBase;

public class TestAliases extends TestNGBase {

    @Test
    public void testAliases() {
        TestContext.getGlobalAliases().put("name", "Misha");
        TestContext.getGlobalAliases().put("age", new AgeCalculator());
        FormPO formPO = new FormPO().fromResource("dataSet.xml");
        System.out.println(formPO);
    }
}
