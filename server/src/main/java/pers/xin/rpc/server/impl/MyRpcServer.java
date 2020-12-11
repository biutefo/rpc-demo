package pers.xin.rpc.server.impl;

import pers.xin.rpc.common.Constants;
import pers.xin.rpc.common.HelloService;
import pers.xin.rpc.common.TestServer;
import pers.xin.rpc.server.Server;
import pers.xin.rpc.provider.HelloServiceImpl;
import pers.xin.rpc.provider.TestServerImpl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("all")
public class MyRpcServer implements Server {


    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private volatile boolean isRunning = false;

    private final ConcurrentHashMap<String, Class> serviceRegister = new ConcurrentHashMap<>();

    private final int port;

    public MyRpcServer(int port) {
        this.port = port;
    }


    @Override
    public void start() throws IOException {

        ServerSocket server = new ServerSocket();
        server.bind(new InetSocketAddress(port));

        System.out.println("Server start");

        try {
            while (true) {
                executorService.execute(new ServiceTask(server.accept()));
            }
        } finally {
            server.close();
        }
    }

    @Override
    public void stop() throws IOException {
        isRunning = false;
        executorService.shutdown();
    }

    @Override
    public <T> void register(Class<T> serviceInterface, Class<? extends T> impl) {
        serviceRegister.put(serviceInterface.getName(), impl);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getPort() {
        return port;
    }

    private class ServiceTask implements Runnable {

        Socket socket;

        public ServiceTask(Socket socket) {
            this.socket = socket;
        }


        @Override
        public void run() {
            ObjectInputStream input = null;
            ObjectOutputStream output = null;

            try {
                input = new ObjectInputStream(socket.getInputStream());

                String serviceName = input.readUTF();
                String methodName = input.readUTF();
                Class<?>[] parameterTypes = (Class<?>[]) input.readObject();

                Object[] arguments = (Object[]) input.readObject();

                Class<?> serviceClass = serviceRegister.get(serviceName);

                if (null == serviceClass) {
                    throw new ClassNotFoundException(serviceName + "not found");
                }

                Method method = serviceClass.getDeclaredMethod(methodName, parameterTypes);

                Object result = method.invoke(serviceClass.newInstance(), arguments);

                output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(result);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public static void main(String[] args) {
        Server server = new MyRpcServer(Constants.PORT);
        try {
            server.register(HelloService.class, HelloServiceImpl.class);
            server.register(TestServer.class, TestServerImpl.class);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
