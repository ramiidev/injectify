package com.ramiidev.injectify.fixtures.cycle;

import com.ramiidev.injectify.shared.annotation.Autowired;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

@Component
public class CycleFirst {
    private CycleSecond second;

    @Autowired
    public void setSecond(CycleSecond second) {
        this.second = second;
    }

    public CycleSecond getSecond() {
        return second;
    }
}
