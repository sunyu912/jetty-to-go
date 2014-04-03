package io.magnum.jetty.server.analyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hd4ar.awscommon.retry.AbortException;

public class VizwebAnalyzer extends BBDSAnalyzer {
    
    private static Logger logger = LoggerFactory.getLogger(VizwebAnalyzer.class);
    
    public void analyzeColorfullness(String id) {
        try {
            syncInputFile(id);
        } catch (AbortException e) {
            logger.error("Failed to get the input file.", e);
        }
    }
}
