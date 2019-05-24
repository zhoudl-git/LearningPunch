package top.zhoudl.designpatterns.proxy.dbroute;

/**
 * @author: zhoudongliang
 * @date: 2019/5/13 15:18
 * @description:
 */
public class OrderDao {

    /**
     * 创建订单
     * @param order
     * @return
     */
    public int insert(Order order){
        System.out.println(" OrderDao 创建 Order = " + order + " 成功!");
        return 1;
    }
}
