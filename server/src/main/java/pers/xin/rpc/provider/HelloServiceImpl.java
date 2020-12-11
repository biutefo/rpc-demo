package pers.xin.rpc.provider;


import pers.xin.rpc.common.HelloService;

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello, "+name;
    }
}
