package com.aliware.tianchi;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Result;

/**
 * ServerStatus
 */
public class ServerStatus {

    private AtomicInteger concurrent = new AtomicInteger(0);
    private int activeConcurrent = 0;
    private int maxActiveConcurrent = 0;
    private int success=0;
    private int totalDelay=0;
    private int recentSuccess=0;
    private int recentDelay=0;
    private int recentError = 0;

    private long startTime;

    public ServerStatus() {
        startTime = System.currentTimeMillis();
    }
    
    public void reset() {
        recentDelay = 0;
        recentSuccess = 0;
        recentError = 0;
    }
    
    public void update(ProviderStatus providerStatus) {
        activeConcurrent = providerStatus.current.get();
        maxActiveConcurrent = providerStatus.maxCurrent;
    }

    public double getWeight() {
        double queuingRate;
        double recentErrorRate;
        double avgRecentDelay;

        if (concurrent.get() == 0) {
            return 0;
        }
        
        if (startTime != -1 && System.currentTimeMillis() - startTime > 20000) {
            startTime = -1;
        }
        
        if (startTime == -1 && concurrent.get() > maxActiveConcurrent*0.95) {
            return Integer.MIN_VALUE;
        }

        if (activeConcurrent == 0) {
            return 0;
        } else {
            queuingRate = activeConcurrent / (double) concurrent.get();
        }

        recentErrorRate = (1 + recentSuccess) / (double) (1 + recentError);

        avgRecentDelay = (1 + recentSuccess) / (double) (1 + recentDelay);
        
        return queuingRate * queuingRate * recentErrorRate;

    }

    public void start(Invocation invocation) {
        invocation.getAttachments().put("start", String.valueOf(System.currentTimeMillis()));
        concurrent.incrementAndGet();
    }

    public void stop(Result result, Invocation invocation) {
        if (concurrent.get() > 0) {
            concurrent.decrementAndGet();
        }

        String status = result.getAttachment("status");
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
        return String.format("%d,%d,%d,%d,%d,%d,%d,%d", activeConcurrent ,concurrent.get() ,maxActiveConcurrent,recentDelay,recentError,recentSuccess,success,totalDelay);
    }
}