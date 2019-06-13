package com.aliware.tianchi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.List;
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

    private int getWeight(Invoker<?> invoker, Invocation invocation) {
        // 获得 weight 配置，即服务权重。默认为 100
        int weight = 100;
        switch (invoker.getUrl().getHost()){
            case "provider-small":
                weight = 100;
                break;
            case "provider-medium":
                weight = 200;
                break;
            case "provider-large":
                weight = 300;
                break;
        }
        return weight;
    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
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
        int[] weights = new int[length];
        // the first invoker's weight
        int firstWeight = getWeight(invokers.get(0), invocation);
        weights[0] = firstWeight;
        // The sum of weights
        int totalWeight = firstWeight;
        int maxIndex = 0;
        for (int i = 1; i < length; i++) {
            int weight = getWeight(invokers.get(i), invocation);
            if (firstWeight < weight){
                firstWeight = weight;
                maxIndex = i;
            }
            // save for later use
            weights[i] = weight;
//            // Sum
//            totalWeight += weight;
//            if (sameWeight && weight != firstWeight) {
//                sameWeight = false;
//            }
        }

//        if (totalWeight > 0 && !sameWeight) {
//            // If (not every invoker has the same weight & at least one invoker's weight>0), select randomly based on totalWeight.
//            int offset = ThreadLocalRandom.current().nextInt(totalWeight);
//            // Return a invoker based on the random value.
//            for (int i = 0; i < length; i++) {
//                offset -= weights[i];
//                if (offset < 0) {
//                    count[i] += 1;
//                    return invokers.get(i);
//                }
//            }
//        }
//        // If all invokers have the same weight value or totalWeight=0, return evenly.
//        int i = ThreadLocalRandom.current().nextInt(length);
//        count[i] += 1;
        return invokers.get(maxIndex);
    }
}
