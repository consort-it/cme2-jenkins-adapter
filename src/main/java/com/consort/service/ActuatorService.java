package com.consort.service;

import com.consort.database.entities.Counter;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActuatorService {

    private static ActuatorService instance = null;
    private static final Map<String, Counter> counterMap = new HashMap<>();
    private static final String COUNTER_A = "counterA";
    private static final String COUNTER_B = "counterB";

    private ActuatorService() {}

    public static ActuatorService getInstance() {
        if(instance == null) {
            instance = new ActuatorService();
        }

        return instance;
    }

    public List<Counter> getCounters(final Response response) {

        response.type("application/json");

        if (counterMap.isEmpty()) {
            counterMap.put(COUNTER_A, new Counter(COUNTER_A, 1));
            counterMap.put(COUNTER_B, new Counter(COUNTER_B, 1));
        } else {
            final Counter counterA = counterMap.get(COUNTER_A);
            counterA.setValue(counterA.getValue() + 1);
            final Counter counterB = counterMap.get(COUNTER_B);
            counterB.setValue(counterB.getValue() + 1);
        }

        List<Counter> counterList = new ArrayList<>();

        for (Map.Entry<String, Counter> entry : counterMap.entrySet()) {
            counterList.add(entry.getValue());
        }

        return counterList;
    }

    public Counter getCounterByName(final String name) {
        final Counter counter = counterMap.get(name);
        if (counter != null) {
            counter.setValue(counter.getValue() + 1);
        }
        return counter;
    }
}
