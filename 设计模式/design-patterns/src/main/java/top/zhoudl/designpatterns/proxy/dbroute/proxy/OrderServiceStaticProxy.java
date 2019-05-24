package top.zhoudl.designpatterns.proxy.dbroute.proxy;

import top.zhoudl.designpatterns.proxy.dbroute.IOrderService;
import top.zhoudl.designpatterns.proxy.dbroute.Order;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: zhoudongliang
 * @date: 2019/5/13 15:29
 * @description:
 */
public class OrderServiceStaticProxy {
    private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
    private IOrderService orderService;

    public OrderServiceStaticProxy(IOrderService orderService) {
        this.orderService = orderService;
    }

    public int createOrder(Order order) {
        before();
        Long time = order.getCreateTime();
        Integer dbRouter = Integer.valueOf(yearFormat.format(new Date(time)));
        System.out.println("静态代理类自动分配到【DB_" + dbRouter + "】数据源处理数据。");
        DynamicDataSourceEntry.set(dbRouter);
        orderService.createOrder(order);
        after();
        return 0;
    }

    private void before() {
        System.out.println("Proxy before method.");
    }

    private void after() {
        System.out.println("Proxy after method.");
    }
}
