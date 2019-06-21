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
    private int resentSuccess=0;
    private int resentDelay=0;
    private int resentError = 0;

    public ServerStatus() {
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                resentDelay = 0;
                resentSuccess = 0;
                resentError = 0;
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
                * (resentSuccess / (double) (1 + resentError))
                * (1 + resentSuccess) / (double) (1 + resentDelay);
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
            resentError++;
        } else {
            success++;
            resentSuccess++;
            long start = Long.parseLong(invocation.getAttachments().get("start"));
            long delay = System.currentTimeMillis() - start;
            totalDelay += delay;
            resentDelay += delay;
        }
    }
}