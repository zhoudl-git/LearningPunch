package top.zhoudl.nettysocketiodemo.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.zhoudl.nettysocketiodemo.pojo.MsgBean;

import javax.annotation.Resource;

/**
 * @author: zhoudongliang
 * @date: 2019/2/14 16:33
 * @description:
 */
@Component
@Slf4j
public class MessageEventHandler {

    private final SocketIOServer socketIoServer;

    @Autowired
    public MessageEventHandler(SocketIOServer server) {
        this.socketIoServer = server;
    }

    @Resource(name = "clientCache")
    private SocketIOClientCache clientCache;

    @Resource(name = "socketIOResponse")
    private SocketIOResponse socketIOResponse;

    @OnConnect
    public void onConnect(SocketIOClient client) {
        System.out.println("建立连接");
    }

    @OnEvent("OnMSG")
    public void onSync(SocketIOClient client, MsgBean bean) {
        System.out.printf("收到消息-from: %s to:%s\n", bean.getFrom(), bean.getTo());
        clientCache.addClient(client, bean);
        SocketIOClient ioClients = clientCache.getClient(bean.getTo());
        System.out.println("clientCache");
        if (ioClients == null) {
            System.out.println("你发送消息的用户不在线");
            return;
        }
        socketIOResponse.sendEvent(ioClients,bean);
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        System.out.println("关闭连接");
    }

}
