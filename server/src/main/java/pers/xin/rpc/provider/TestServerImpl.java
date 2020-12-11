package pers.xin.rpc.provider;



import pers.xin.rpc.common.TestServer;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestServerImpl implements TestServer {
    private static final SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm");

    @Override
    public String getTime() {
        return simpleDateFormat.format(new Date());
    }
}
