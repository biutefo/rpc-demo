package pers.xin.rpc.server;

import java.io.IOException;

public interface Server {

    void start() throws IOException;

    void stop() throws IOException;

    <T> void register(Class<T> serviceInterface, Class<? extends T> impl);

    boolean isRunning();

    int getPort();

}
