package com.aliware.tianchi;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Result;

/**
 * ServerStatus
 */
public class ServerStatus {

    private int concurrent = 0;
    private int activeConcurrent = 0;
    private int maxActiveConcurrent = 0;
    private int success=0;
    private int totalDelay=0;
    private int recentSuccess=0;
    private int recentDelay=0;
    private int recentError = 0;

    public ServerStatus() {
        
    }
    
    public void reset() {
        recentDelay = 0;
        recentSuccess = 0;
        recentError = 0;
    }
    
    public void update(ProviderStatus providerStatus) {
        activeConcurrent = providerStatus.current;
        maxActiveConcurrent = providerStatus.maxCurrent;
    }

    public double getWeight() {
        double queuingRate;
        double recentErrorRate;
        double avgRecentDelay;

        if (concurrent == 0 ) {
            return 0;
        }

        if (activeConcurrent == 0) {
            return 0;
        } else {
            queuingRate = 1 / (double) (concurrent - activeConcurrent + 1);
        }

        recentErrorRate = (1 + recentSuccess) / (double) (1 + recentError);

        avgRecentDelay = (1 + recentSuccess) / (double) (1 + recentDelay);
        
        
        return queuingRate * recentErrorRate * avgRecentDelay * avgRecentDelay;

    }

    public void start(Invocation invocation) {
        invocation.getAttachments().put("start", String.valueOf(System.currentTimeMillis()));
        concurrent++;
    }

    public void stop(Result result, Invocation invocation) {
        if (concurrent > 0) {
            concurrent--;
        }

        String status = invocation.getAttachment("status");
        if (status != null) {
            ProviderStatus providerStatus = ProviderStatus.decode(status);
            if (providerStatus != null) {
                update(providerStatus);
            }
        }

        if (result.hasException() || result.getValue() == null || result.getValue().equals("")) {
            recentError++;
        } else {
            success++;
            recentSuccess++;
            String startString = invocation.getAttachment("start");
            if (startString != null) {
                long start = Long.parseLong(startString);
                long delay = System.currentTimeMillis() - start;
                totalDelay += delay;
                recentDelay += delay;
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%d,%d,%d,%d,%d", activeConcurrent,concurrent,maxActiveConcurrent,recentDelay,recentError,recentSuccess,success,totalDelay);
    }
}