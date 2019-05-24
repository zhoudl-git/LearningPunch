package top.zhoudl.designpatterns.proxy.dbroute;

import top.zhoudl.designpatterns.proxy.dbroute.proxy.OrderServiceStaticProxy;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: zhoudongliang
 * @date: 2019/5/13 15:38
 * @description:
 */
public class DbRouteTest {

    public static void main(String[] args) {
        try {
            Order order = new Order();
            // Date today = new Date();
            // order.setCreateTime(today.getTime());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            Date daApplicationContextAwarete = sdf.parse("2017/02/01");
            order.setCreateTime(System.currentTimeMillis());
            OrderServiceStaticProxy orderServiceStaticProxy = new OrderServiceStaticProxy(new IOrderServiceImpl());
            orderServiceStaticProxy.createOrder(order);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
