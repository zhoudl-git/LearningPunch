package top.zhoudl.nettysocketiodemo.handler;

import com.corundumstudio.socketio.SocketIOClient;
import org.springframework.stereotype.Service;
import top.zhoudl.nettysocketiodemo.pojo.MsgBean;

/**
 * @author: zhoudongliang
 * @date: 2019/2/14 17:17
 * @description:
 */
@Service("socketIOResponse")
public class SocketIOResponse {

    public void sendEvent(SocketIOClient client, MsgBean bean) {
        System.out.println("推送消息");
        client.sendEvent("OnMSG", bean);
    }
}