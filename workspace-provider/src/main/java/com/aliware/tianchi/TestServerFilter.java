package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

/**
 * @author daofeng.xjf
 *
 * 服务端过滤器
 * 可选接口
 * 用户可以在服务端拦截请求和响应,捕获 rpc 调用时产生、服务端返回的已知异常。
 */
@Activate(group = Constants.PROVIDER)
public class TestServerFilter implements Filter {

    private static boolean first = true;

    public static ProviderStatus providerStatus = null;

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try {
            if (providerStatus == null) {
                synchronized (TestServerFilter.class) {
                    if (providerStatus == null && invocation.getMethodName().equals("hash")) {
                        providerStatus = new ProviderStatus(invoker.getUrl().getPort(), 0, 0);
                    }
                }
            }

            if (invocation.getMethodName().equals("hash")) {
                providerStatus.current.incrementAndGet();
                if (providerStatus.current.get() > providerStatus.maxCurrent) {
                    providerStatus.maxCurrent = providerStatus.current.get();
                }
            }

            
            Result result = invoker.invoke(invocation);
            return result;
        }catch (Exception e){
            throw e;
        }

    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        if (invocation.getMethodName().equals("hash")) {
            if (first) {
                first = false;
                result.getAttachments().put(Constants.THREADS_KEY,
                        invoker.getUrl().getParameter(Constants.THREADS_KEY, Constants.DEFAULT_THREADS) + "");
            }

                providerStatus.current.decrementAndGet();
        }
        if (providerStatus != null) {
            result.getAttachments().put("status", providerStatus.encode());
        }
        
        
        return result;
    }

}
