package vision;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;

public class MyWebSocketServer extends WebSocketServer {

    public MyWebSocketServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        System.out.println("MyWebSocketServer(int port) port = " + port);
    }

    public MyWebSocketServer(InetSocketAddress address) {
        super(address);
        System.out.println("MyWebSocketServer(InetSocketAddress address)");
    }

    public MyWebSocketServer(int port, Draft_6455 draft) {
        super(new InetSocketAddress(port), Collections.<Draft>singletonList(draft));
        System.out.println("MyWebSocketServer(int port, Draft_6455 draft) port = " + port);
    }

    /**
     * WebSocketServer启动成功的回调，不代表客户端和服务端连接成功
     */
    @Override
    public void onStart() {
        System.out.println("onStart");
        setConnectionLostTimeout(100);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("onOpen : " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("onMessage : " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + " String = " + message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        System.out.println("onMessage : " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + " ByteBuffer = " + message.toString());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("onClose : " + conn.getRemoteSocketAddress().getAddress().getHostAddress()
                + " code = " + code
                + " reason = " + reason
                + " remote = " + remote);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("onError : " + conn.getRemoteSocketAddress().getAddress().getHostAddress()
                + " Exception = " + ex.toString());
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        int port = 8887;
        MyWebSocketServer webSocketServer = new MyWebSocketServer(port);
        webSocketServer.start();
        System.out.println("MyWebSocketServer start");

        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String in = sysin.readLine();
            // 发送两次数据
            webSocketServer.broadcast(in);
            webSocketServer.broadcast(in.getBytes());
            if (in.equals("exit")) {
                webSocketServer.stop(1000);
                break;
            }
        }
    }
}

