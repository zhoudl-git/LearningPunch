package top.zhoudl.nettysocketiodemo;

import java.lang.reflect.Field;

/**
 * @author: zhoudongliang
 * @date: 2019/2/21 17:28
 * @description:
 */
public class SwapIntegerValue {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        Integer a = 1, b = 2;
        System.out.println("交换前：a = " + a + ", b = " + b);
        swap(a,b);
        System.out.println("交换后：a = " + a + ", b = " + b);
    }

    public static void swap(Integer i, Integer j) throws NoSuchFieldException, IllegalAccessException {
        Field value = Integer.class.getDeclaredField("value");
        value.setAccessible(true);
        int temp = new Integer(i.intValue());
        //int temp = i.intValue();
        value.setInt(i,j.intValue());
        value.setInt(j,temp);
    }
}
