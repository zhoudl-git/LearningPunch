package top.zhoudl.designpatterns.singleton;

/**
 * @author: zhoudongliang
 * @date: 2019/5/10 10:40
 * @description: 使用枚举实现单例
 */
enum EnumSingleton {

    INSTANCE;

    private Object data;

    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }
    public static EnumSingleton getInstance(){
        return INSTANCE;
    }

}
