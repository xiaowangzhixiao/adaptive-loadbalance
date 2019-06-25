package com.aliware.tianchi;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Result;

/**
 * ServerStatus
 */
public class ServerStatus {

    public AtomicInteger concurrent = new AtomicInteger(0);
    public int activeConcurrent = 0;
    public int maxThreads = 0;
    public int maxActiveConcurrent = 0;
    public int success=0;
    public int totalDelay=0;
    public int recentSuccess=0;
    public int recentDelay=0;
    public int recentError = 0;

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
        double avgRecentDelay;

        if (maxThreads != 0 && concurrent.get() > maxThreads*0.9) {
            return Integer.MIN_VALUE;
        }
        
        if (activeConcurrent == 0 || concurrent.get() == 0) {
            return 0;
        } else {
            queuingRate = activeConcurrent / (double) concurrent.get();
        }

        avgRecentDelay = (1 + recentSuccess) / (double) (1 + recentDelay);
        
        return queuingRate * queuingRate * avgRecentDelay ;

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

        String maxThreads = result.getAttachment(Constants.THREADS_KEY);
        if (maxThreads != null) {
            this.maxThreads = Integer.parseInt(maxThreads);
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
        return String.format("%f,%d,%d,%d,%d,%d,%d,%d,%d,%d",
                getWeight(),
                activeConcurrent, 
                concurrent.get(), 
                maxActiveConcurrent, 
                recentSuccess,
                recentDelay, 
                recentError, 
                success, 
                totalDelay, 
                maxThreads);
    }
}