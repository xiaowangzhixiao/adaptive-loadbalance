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
    public int lastPanding = 0;
    public int activeConcurrent = 0;
    public int maxThreads = 0;

    public ServerStatus() {
    }
    
    public void update(ProviderStatus providerStatus) {
        lastPanding = concurrent.get() - activeConcurrent;
        activeConcurrent = providerStatus.current.get();
    }

    public void start(Invocation invocation) {
        concurrent.incrementAndGet();
    }

    public void stop(Result result, Invocation invocation) {

        String status = result.getAttachment("status");
        if (status != null) {
            concurrent.decrementAndGet();
            ProviderStatus providerStatus = ProviderStatus.decode(status);
            if (providerStatus != null) {
                update(providerStatus);
            }
        }

        String maxThreads = result.getAttachment(Constants.THREADS_KEY);
        if (maxThreads != null) {
            this.maxThreads = Integer.parseInt(maxThreads);
        }
    }

    @Override
    public String toString() {
        return String.format("%d,%d,%d",
                activeConcurrent, 
                concurrent.get(),
                maxThreads
                );
    }
}