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
 * 客户端过滤器
 * 可选接口
 * 用户可以在客户端拦截请求和响应,捕获 rpc 调用时产生、服务端返回的已知异常。
 */
@Activate(group = Constants.CONSUMER)
public class TestClientFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try{
            int index = UserLoadBalance.index.get(invoker.getUrl().getHost());
            UserLoadBalance.concurrentNum.getAndIncrement(index);
            Result result = invoker.invoke(invocation);
            return result;
        }catch (Exception e){
            throw e;
        }

    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        int index = UserLoadBalance.index.get(invoker.getUrl().getHost());
        UserLoadBalance.concurrentNum.getAndDecrement(index);
        if (result.hasException()) {
            System.out.println("onResponse :" + result.getException().getMessage());

            UserLoadBalance.concurrentMaxNum.set(index, UserLoadBalance.concurrentNum.get(index));
            if (UserLoadBalance.second) {
                UserLoadBalance.weight.set(index, 0); 
            } else {
                UserLoadBalance.weight.set(index, UserLoadBalance.concurrentNum.get(index) - 5); 
            }
            
            boolean flag = false;
            for (int i = 0; i < UserLoadBalance.concurrentMaxNum.length(); i++) {
                if (UserLoadBalance.concurrentMaxNum.get(i) == 0) {
                    UserLoadBalance.weight.set(i, 1);
                    flag = true;
                }
            }

            if (!flag && UserLoadBalance.second) {
                synchronized (UserLoadBalance.second) {
                    UserLoadBalance.second=false;
                    for (int j = 0; j < UserLoadBalance.weight.length(); j++) {
                        UserLoadBalance.weight.set(j, UserLoadBalance.concurrentMaxNum.get(j));
                    }
                }
                
            }
            
            System.out.println(UserLoadBalance.weight.toString());
        }
        
        return result;
    }
}
