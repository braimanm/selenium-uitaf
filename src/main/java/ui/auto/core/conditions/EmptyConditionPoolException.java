package ui.auto.core.conditions;

public class EmptyConditionPoolException extends RuntimeException {
    @Override
    public String getMessage() {
        return "No conditions were specified. Please add conditions first.";
    }
}
