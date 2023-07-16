package top.guoziyang.test;

import top.guoziyang.rpc.annotation.ServiceScan;
import top.guoziyang.rpc.serializer.CommonSerializer;
import top.guoziyang.rpc.transport.RpcServer;
import top.guoziyang.rpc.transport.socket.server.SocketServer;

/**
 * 测试用服务提供方（服务端）
 *
 * @author ziyang
 */
@ServiceScan
public class SocketTestServer {
    //G:\JavaDemo\My-RPC-Framework-master\test-server\src\main\java\top\guoziyang\test\SocketTestServer.java
    public static void main(String[] args) {
        //将该启动文件下包含的@Service的文件注册到本地map和nacos中（...）
        RpcServer server = new SocketServer("127.0.0.1", 9998, CommonSerializer.HESSIAN_SERIALIZER);
        server.start();
    }

}
