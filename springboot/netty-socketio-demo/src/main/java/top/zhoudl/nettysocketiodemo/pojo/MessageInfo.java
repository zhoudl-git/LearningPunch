package top.zhoudl.nettysocketiodemo.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * message_info
 * @author zhoudongliang
 */
@Data
public class MessageInfo implements Serializable {

    private static final long serialVersionUID = 2573296678965819439L;

    private Integer id;

    /**
     * 消息要跳转的URL
     */
    //@NotNull(message = "消息参数 URL 不能为空")
    private String url;

    /**
     * 消息操作时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    /**
     * 消息接收方 用户ID
     */
    private Integer receiveUserId;

    /**
     * 消息接收方 角色ID
     */
    private Integer receiveRoleId;

    /**
     * 消息发送方 用户ID
     */
    private Integer sendUserId;

    /**
     * 消息类型 100 经销商方消息 200 平台方消息 300 系统广播消息
     */
    @NotNull(message = "消息参数 消息类型(msgType) 不能为空")
    private Integer msgType;

    /**
     * 消息列表显示内容
     */
    @NotNull(message = "消息参数 消息列表显示内容(msg) 不能为空")
    private String msg;

    /**
     * 是否已读 0 未读 1 已读
     */
    private Integer status;

    /**
     * 更新时间
     */
    private Date updateTime;

}