package rpc;

import lombok.*;

@Getter
@Setter
@ToString
public class RPCResponse<T> {
    private String id;
    private T result;
    private String error;
}
