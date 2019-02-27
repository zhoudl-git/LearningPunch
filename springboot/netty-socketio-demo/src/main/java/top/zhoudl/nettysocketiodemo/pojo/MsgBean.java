package top.zhoudl.nettysocketiodemo.pojo;

import lombok.Data;

/**
 * @author: zhoudongliang
 * @date: 2019/2/14 17:14
 * @description:
 */
@Data
public class MsgBean {
    private String from;
    private String to;
    private String content;
}
