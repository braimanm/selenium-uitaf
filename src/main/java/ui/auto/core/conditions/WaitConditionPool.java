package ui.auto.core.conditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class WaitConditionPool {
    private long time_out;
    private Map<String, WaitCondition> conditions = new HashMap<>();
    private List<String> fulfilledConditions;

    public WaitConditionPool(long time_out) {
        this.time_out = time_out;
    }

    public WaitCondition add(String name, WaitCondition condition) {
        conditions.put(name, condition);
        return condition;
    }

    public List<String> getFulfilledConditions() {
        return fulfilledConditions;
    }

    public boolean waitForFirst() {
        return check (() -> {
            for (String name : conditions.keySet()) {
                if (conditions.get(name).evaluate()) {
                    fulfilledConditions.add(name);
                    return true ;
                }
            }
            return false;
        });
    }

    public boolean waitForAll() {
        return check(this::forAll);
    }

    public boolean waitCustom(BiFunction<Map<String, WaitCondition>, List<String>, Boolean> check) {
        return check(() -> check.apply(conditions, fulfilledConditions));
    }

    private boolean check(WaitCondition condition ) {
        fulfilledConditions = new ArrayList<>();
        if (conditions.isEmpty()) {
            throw new EmptyConditionPoolException();
        }
        long t_o = System.currentTimeMillis() + time_out;
        do {
            if (condition.evaluate()) return true;
        } while (System.currentTimeMillis() < t_o);
        return false;
    }

    private boolean forAll() {
        for (String name : conditions.keySet()) {
            if (!fulfilledConditions.contains(name)) {
                if (conditions.get(name).evaluate()) {
                    fulfilledConditions.add(name);
                    if (fulfilledConditions.size() == conditions.size()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
