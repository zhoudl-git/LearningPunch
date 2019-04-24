package top.zhoudl.nettysocketiodemo.pojo;

import lombok.Data;

/**
 * @author: zhoudongliang
 * @date: 2019/2/14 16:32
 * @description:
 */
@Data
public class ReportParam {
    /**
     * IMEI码
     */
    private String imei;
    /**
     * 位置
     */
    private String location;
}
