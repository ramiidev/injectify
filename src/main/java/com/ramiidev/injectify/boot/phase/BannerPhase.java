package com.ramiidev.injectify.boot.phase;

import com.ramiidev.injectify.boot.BootstrapContext;
import com.ramiidev.injectify.boot.BootstrapPhase;
import com.ramiidev.injectify.boot.banner.support.BannerPrinter;
import com.ramiidev.injectify.infrastructure.logging.support.DeferredLogFactory;
import com.ramiidev.injectify.infrastructure.logging.support.DeferredLogger;

public final class BannerPhase implements BootstrapPhase {

    private final DeferredLogger logger = DeferredLogFactory.getLog(BannerPhase.class);

    @Override
    public void run(BootstrapContext context) {
        BannerPrinter printer = new BannerPrinter();
        printer.print();
    }
}
