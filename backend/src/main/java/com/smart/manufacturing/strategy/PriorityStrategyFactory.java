package com.smart.manufacturing.strategy;

import com.smart.manufacturing.enums.Priority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory and Registry for polymorphic PriorityStrategy instances.
 * Demonstrates Collections (EnumMap) and Object-Oriented Polymorphism.
 */
@Component
public class PriorityStrategyFactory {

    private final Map<Priority, PriorityStrategy> strategyMap = new EnumMap<>(Priority.class);

    public PriorityStrategyFactory() {
        this(List.of(
            new UrgentPriorityStrategy(),
            new HighPriorityStrategy(),
            new NormalPriorityStrategy(),
            new LowPriorityStrategy()
        ));
    }

    @Autowired
    public PriorityStrategyFactory(List<PriorityStrategy> strategies) {
        for (PriorityStrategy strategy : strategies) {
            strategyMap.put(strategy.getPriority(), strategy);
        }
    }

    public PriorityStrategy getStrategy(Priority priority) {
        if (priority == null) {
            priority = Priority.NORMAL;
        }
        PriorityStrategy strategy = strategyMap.get(priority);
        if (strategy == null) {
            return strategyMap.get(Priority.NORMAL);
        }
        return strategy;
    }
}
