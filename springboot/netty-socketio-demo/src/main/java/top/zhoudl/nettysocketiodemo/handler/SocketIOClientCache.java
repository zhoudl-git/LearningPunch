package top.zhoudl.nettysocketiodemo.handler;

import com.corundumstudio.socketio.SocketIOClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import top.zhoudl.nettysocketiodemo.pojo.MsgBean;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: zhoudongliang
 * @date: 2019/2/14 17:13
 * @description:
 */
@Service("clientCache")
public class SocketIOClientCache {

    private Map<String, SocketIOClient> clients = new ConcurrentHashMap<>();

    /**
     * 用户发送消息添加
     * @param client
     * @param msgBean
     */
    public void addClient(SocketIOClient client, MsgBean msgBean) {
        clients.put(msgBean.getFrom(), client);
    }

    /**
     * 用户退出时移除
     * @param msgBean
     */
    public void remove(MsgBean msgBean) {
        clients.remove(msgBean.getFrom());
    }

    /**
     * 获取所有
     * @param to
     * @return
     */
    public SocketIOClient getClient(String to) {
        return clients.get(to);
    }
}
