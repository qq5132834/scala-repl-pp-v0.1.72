package vision;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 * 客户端
 */
public class MyWebSocketClient extends WebSocketClient {

    /**
     * 构造方法
     *
     * @param serverUri new URI("ws://127.0.0.1:8887/")
     */
    public MyWebSocketClient(URI serverUri) {
        super(serverUri);
        System.out.println("MyWebSocketClient");
    }

    /**
     * 连接成功的回调
     *
     * @param serverHandshake The handshake of the websocket instance
     */
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("onOpen: getContent = " + serverHandshake.getContent()
                + ", getHttpStatus = " + serverHandshake.getHttpStatus()
                + ", getHttpStatusMessage = " + serverHandshake.getHttpStatusMessage());
    }

    /**
     * 收到来自服务端的消息 字符串
     *
     * @param s The UTF-8 decoded message that was received.
     */
    @Override
    public void onMessage(String s) {
        System.out.println("onMessage: String = " + s);
    }

    /**
     * 收到来自服务端的消息 字节数组
     * @param bytes The binary message that was received.
     */
    @Override
    public void onMessage(ByteBuffer bytes) {
        super.onMessage(bytes);
        System.out.println("onMessage: ByteBuffer = " + bytes.toString());
    }

    /**
     * 连接关闭的回调
     * @param code   The codes can be looked up here: {@link org.java_websocket.framing.CloseFrame}
     * @param reason Additional information string
     * @param remote Returns whether or not the closing of the connection was initiated by the remote
     *               host.
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("onClose: code = " + code
                + ", reason = " + reason
                + ", remote = " + remote);
    }

    /**
     * 连接错误的回调
     * @param e The exception causing this error
     */
    @Override
    public void onError(Exception e) {
        System.out.println("onError: " + e.toString());
    }

    public static void main(String[] args) throws IOException {

        try {
            //实例WebSocketClient对象，并连接到WebSocket服务端
            MyWebSocketClient client = new MyWebSocketClient(new URI("ws://127.0.0.1:8887/"));
            client.connect();
            //等待服务端响应


            while (!client.getReadyState().equals(ReadyState.OPEN)) {
                System.out.println("连接中···请稍后");
                Thread.sleep(1000);
            }

            BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String in = sysin.readLine();
                if (in.equals("exit")) {
                    client.close();
                    break;
                }
                client.send(in);
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

