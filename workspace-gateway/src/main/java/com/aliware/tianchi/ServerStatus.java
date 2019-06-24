package com.aliware.tianchi;

import java.util.Timer;
import java.util.TimerTask;

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

        if (concurrent == 0) {
            return 0;
        }

        if (activeConcurrent == 0) {
            // queuingRate = 1 / (double) concurrent;
            return 0;
        } else {
            queuingRate = activeConcurrent / (double) concurrent;
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
        
        if (result.hasException() || result.getValue() == null || result.getValue().equals("")) {
            recentError++;
        } else {
            success++;
            recentSuccess++;
            if (invocation.getAttachments().get("start") != null) {
                long start = Long.parseLong(invocation.getAttachments().get("start"));
                long delay = System.currentTimeMillis() - start;
                totalDelay += delay;
                recentDelay += delay;
            }
        }
    }
}