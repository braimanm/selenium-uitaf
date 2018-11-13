package ui.auto.core.conditions;

@FunctionalInterface
public interface WaitCondition {
    boolean evaluate();
}
