package pers.xin.rpc.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class InvokerProtocol implements Serializable {

    private String className;
    private String methodName;
    private Class<?>[] params;
    private Object[] values;

}
