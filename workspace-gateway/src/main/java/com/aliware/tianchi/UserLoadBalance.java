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
            synchronized (statusMap) {
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
        double maxWeight = 0.0;
        int maxIndex = -1;
        for (int i = 0; i < invokers.size(); i++) {
            // System.out.println("get Weight "+i);
            double weights = statusMap.get(invokers.get(i).getUrl().toIdentityString()).getWeight();
            if (weights > maxWeight) {
                maxWeight = weights;
                maxIndex = i;
            }
            if (weights == 0) {
                maxIndex = -1;
                break;
            }
        }

        if (maxIndex == -1) {
            maxIndex = ThreadLocalRandom.current().nextInt(invokers.size());
        }

        // System.out.println("maxIndex = ,"+maxIndex);
        return invokers.get(maxIndex);
    }
}
