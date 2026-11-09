package com.ramiidev.injectify.core.diagnostic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingFailureReporter implements FailureAnalysisReporter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFailureReporter.class);

    @Override
    public void report(FailureAnalysis analysis) {
        String logMessage = """
        
        ***************************
        APPLICATION FAILED TO START
        ***************************
        
        Description:
        %s
        
        Action:
        %s
        """.formatted(analysis.description(), analysis.action());

        log.error(logMessage, analysis.cause());
    }
}
