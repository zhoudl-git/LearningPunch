package top.zhoudl.nettysocketiodemo.handler;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Socket IO 启动器
 * @author: zhoudongliang
 * @date: 2018/11/5 19:38
 * @description: SpringBoot应用启动之后执行
 */
@Component
@Order(value=1)
@Slf4j
public class ServerRunner implements CommandLineRunner {

    private final SocketIOServer server;

    @Autowired
    public ServerRunner(SocketIOServer server) {
        this.server = server;
    }

    @Override
    public void run(String... args){
        server.start();
        log.info("socket.io启动成功!");
    }
}
