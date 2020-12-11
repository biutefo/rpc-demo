package pers.xin.rpc.client;

import pers.xin.rpc.common.Constants;
import pers.xin.rpc.common.HelloService;
import pers.xin.rpc.common.TestServer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RPCClient {

    @SuppressWarnings({"uncheckd", "unused"})
    public static <T> T getRemoteProxy(final Class<T> serviceInterface, String host, int port) {
        Object o = Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, (proxy, method, args) -> {
            Socket socket = null;
            ObjectOutputStream output = null;
            ObjectInputStream input = null;

            try {
                long time = System.currentTimeMillis();
                socket = new Socket();
                socket.connect(new InetSocketAddress(InetAddress.getByName(host), port));
                System.out.println("connect time:" + (System.currentTimeMillis() - time));
                output = new ObjectOutputStream(socket.getOutputStream());
                output.writeUTF(serviceInterface.getName());
                output.writeUTF(method.getName());

                output.writeObject(method.getParameterTypes());
                output.writeObject(args);

                time = System.currentTimeMillis();
                input = new ObjectInputStream(socket.getInputStream());

                System.out.println("rpc time:" + (System.currentTimeMillis() - time));

                return input.readObject();
            } finally {
                if (socket != null) {
                    socket.close();
                }
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            }
        });
        return serviceInterface.cast(o);
    }


    public static void main(String[] args) {
        HelloService helloService = RPCClient.getRemoteProxy(HelloService.class, Constants.HOST, Constants.PORT);
        long time = System.currentTimeMillis();
        System.out.println(helloService.sayHello("xin"));
        long used = System.currentTimeMillis() - time;
        System.out.println("usedTime:" + used);
        TestServer testServer = RPCClient.getRemoteProxy(TestServer.class, Constants.HOST, Constants.PORT);
        System.out.println(testServer.getTime());
    }
}
