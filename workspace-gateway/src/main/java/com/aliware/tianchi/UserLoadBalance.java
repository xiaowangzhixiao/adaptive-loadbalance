package com.aliware.tianchi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
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

    private Timer timer = new Timer();

    public UserLoadBalance() {
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                LocalTime time = LocalTime.now();
                for (Entry<Integer, ServerStatus> serverStatus : statusMap.entrySet()) {
                    System.out.println(time+" "+ serverStatus.getKey().toString() + ":" + serverStatus.getValue().toString());
                    serverStatus.getValue().reset();
                }
            }
        }, 300, 1000);
    }

    public static Map<Integer, ServerStatus> statusMap = new HashMap<>();

    private static <T> void init(List<Invoker<T>> invokers) {
        invokers.forEach(x->statusMap.put(x.getUrl().getPort(), new ServerStatus()));
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
            double weight = statusMap.get(invokers.get(i).getUrl().getPort()).getWeight();
            if (weight >= maxWeight) {
                maxWeight = weight;
                maxIndex = i;
            }
            if (weight == 0) {
                maxIndex = -1;
                break;
                // return invokers.get(i);
            }
        }

        if (maxIndex == -1) {
            maxIndex = ThreadLocalRandom.current().nextInt(invokers.size());
        }

        return invokers.get(maxIndex);
    }
}
