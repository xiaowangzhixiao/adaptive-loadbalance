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
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                recentDelay = 0;
                recentSuccess = 0;
                recentError = 0;
            }
        }, 300, 3000);
    }
    
    public void update(ProviderStatus providerStatus) {
        activeConcurrent = providerStatus.current;
        maxActiveConcurrent = providerStatus.maxCurrent;
    }

    public double getWeight() {
        if (concurrent == 0) {
            return 0;
        }
        return 1 / (double) concurrent * success / (double) (1 + totalDelay)
                * (recentSuccess / (double) (1 + recentError))
                * (1 + recentSuccess) / (double) (1 + recentDelay)* (1 + recentSuccess) / (double) (1 + recentDelay);
        // return 1 / (double) (1 + resentError)*1000;
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
            long start = Long.parseLong(invocation.getAttachments().get("start"));
            long delay = System.currentTimeMillis() - start;
            totalDelay += delay;
            recentDelay += delay;
        }
    }
}