package com.ramiidev.injectify.fixtures.cycle;

import com.ramiidev.injectify.shared.annotation.Autowired;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

@Component
public class CycleSecond {
    private CycleFirst first;

    @Autowired
    public void setFirst(CycleFirst first) {
        this.first = first;
    }

    public CycleFirst getFirst() {
        return first;
    }
}
