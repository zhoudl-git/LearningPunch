package top.zhoudl.designpatterns.proxy.dbroute.proxy;

/**
 * @author: zhoudongliang
 * @date: 2019/5/13 15:27
 * @description:
 */
public class DynamicDataSourceEntry {

    // 默认数据源
    public final static String DEFAULT_SOURCE = null;

    private final static ThreadLocal<String> local = new ThreadLocal<>();

    private DynamicDataSourceEntry() {
    }

    /**
     * 清空数据源
     */
    public static void clear() {
        local.remove();
    }

    /**
     * 获取当前正在使用的数据源名字
     *
     * @return String
     */
    public static String get() {
        return local.get();
    }

    /**
     * 还原当前切面的数据源
     */
    public static void restore() {
        local.set(DEFAULT_SOURCE);
    }

    /**
     * 设置已知名字的数据源
     *
     * @param source
     */
    public static void set(String source) {
        local.set(source);
    }

    /**
     * 根据年份动态设置数据源
     *
     * @param year
     */
    public static void set(int year) {
        local.set("DB_" + year);
    }
}
