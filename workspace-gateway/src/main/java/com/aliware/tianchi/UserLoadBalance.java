package com.aliware.tianchi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author daofeng.xjf
 *
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance {

    public static ConcurrentHashMap<String, ServerStatus> statusMap = new ConcurrentHashMap<>();

    private static <T> void init(List<Invoker<T>> invokers) {
        invokers.forEach(x->statusMap.put(x.getUrl().toIdentityString(), new ServerStatus()));
    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        // 初始化统计
        if (statusMap.size() == 0) {
            synchronized (this) {
                if (statusMap.size() == 0) {
                    init(invokers);
                }
            }
        }
        
        if (invokers == null || invokers.isEmpty()) {
            return null;
        }
        if (invokers.size() == 1) {
            return invokers.get(0);
        }

        return doSelect(invokers, url, invocation);
    }

    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        // Number of invokers
        int length = invokers.size();
        // Every invoker has the same weight?
        boolean sameWeight = true;
        // the weight of every invokers
        double[] weights = new double[length];
        // the first invoker's weight
        double firstWeight = statusMap.get(invokers.get(0).getUrl().toIdentityString()).getWeight();
        weights[0] = firstWeight;
        // The sum of weights
        double totalWeight = firstWeight;
        for (int i = 1; i < length; i++) {
            double weight = statusMap.get(invokers.get(0).getUrl().toIdentityString()).getWeight();
            // save for later use
            weights[i] = weight;
            // Sum
            totalWeight += weight;
            if (sameWeight && weight != firstWeight) {
                sameWeight = false;
            }
            if (weight == 0) {
                sameWeight = true;
            }
        }
        if (totalWeight > 0 && !sameWeight) {
            System.out.println("weights:" + weights);
            // If (not every invoker has the same weight & at least one invoker's weight>0), select randomly based on totalWeight.
            double offset = ThreadLocalRandom.current().nextDouble(totalWeight);
            // Return a invoker based on the random value.
            for (int i = 0; i < length; i++) {
                offset -= weights[i];
                if (offset < 0) {
                    return invokers.get(i);
                }
            }
        }
        // If all invokers have the same weight value or totalWeight=0, return evenly.
        return invokers.get(ThreadLocalRandom.current().nextInt(length));
    }
}
