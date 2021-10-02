package rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RPCRequest {
    private String id;
    private String method;
    private List<Object> params;
}
