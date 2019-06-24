package com.aliware.tianchi;

import org.apache.dubbo.rpc.listener.CallbackListener;

/**
 * @author daofeng.xjf
 *
 * 客户端监听器
 * 可选接口
 * 用户可以基于获取获取服务端的推送信息，与 CallbackService 搭配使用
 *
 */
public class CallbackListenerImpl implements CallbackListener {

    @Override
    public void receiveServerMsg(String msg) {
        System.out.println(msg);
        ProviderStatus providerStatus = ProviderStatus.decode(msg);
        if (providerStatus != null && UserLoadBalance.statusMap.size() != 0) {
            UserLoadBalance.statusMap.get(providerStatus.name).update(providerStatus);
        }
    }

}
