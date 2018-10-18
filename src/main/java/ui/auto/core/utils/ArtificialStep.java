package ui.auto.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.events.MakeAttachmentEvent;
import ru.yandex.qatools.allure.events.StepFailureEvent;
import ru.yandex.qatools.allure.events.StepFinishedEvent;
import ru.yandex.qatools.allure.events.StepStartedEvent;
import ui.auto.core.support.TestContext;

import java.util.ArrayList;
import java.util.List;

public interface ArtificialStep {

    default Throwable executeStep(String stepTitle, long expectedExecutionTime) {
        final Logger LOG = LoggerFactory.getLogger(ArtificialStep.class);
        long execTime;
        long realExecTime = System.currentTimeMillis();
        if (expectedExecutionTime == 0) {
            expectedExecutionTime = TestContext.getTestProperties().getPageTimeout() * 1000;
        }
        List<MakeAttachmentEvent> attachmentEvents = new ArrayList<>();

        try {
            execTime = stepBody(attachmentEvents);
        } catch (Exception e) {
            LOG.error("The following exception was thrown:\n", e);
            Allure.LIFECYCLE.fire(new StepStartedEvent("").withTitle(stepTitle + " (Exception was triggered)"));
            for (MakeAttachmentEvent attachmentEvent : attachmentEvents) {
                Allure.LIFECYCLE.fire(attachmentEvent);
            }
            Allure.LIFECYCLE.fire(new StepFailureEvent().withThrowable(e));
            Allure.LIFECYCLE.fire(new StepFinishedEvent());
            return e;
        }
        realExecTime = System.currentTimeMillis() - realExecTime;
        if (execTime < 0) {
            execTime = realExecTime;
        }
        Allure.LIFECYCLE.fire(new StepStartedEvent("").withTitle(stepTitle + " (" + execTime / 1000f + "s)"));
        for (MakeAttachmentEvent attachmentEvent : attachmentEvents) {
            Allure.LIFECYCLE.fire(attachmentEvent);
        }
        Throwable throwable = null;
        if (execTime > expectedExecutionTime) {
            String msg = "ComparisonFailure: expected:<[" + expectedExecutionTime + "]> but was:<[" + execTime + "]>";
            throwable = new AssertionError(msg);
            LOG.error("The following exception was thrown:\n", throwable);
            Allure.LIFECYCLE.fire(new StepFailureEvent().withThrowable(throwable));
        }
        LOG.info("Artificial step \"" + stepTitle + "\" ended in " + execTime + "ms");
        Allure.LIFECYCLE.fire(new StepFinishedEvent());
        return throwable;
    }

    long stepBody(List<MakeAttachmentEvent> attchments) throws Exception;

}
