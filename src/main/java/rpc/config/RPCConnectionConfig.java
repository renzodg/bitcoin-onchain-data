package rpc.config;

import lombok.Getter;

import java.util.Properties;

@Getter
public class RPCConnectionConfig {
    private final String rpcHost;
    private final int rpcPort;
    private final String rpcUser;
    private final String  rpcPassword;

    public RPCConnectionConfig(Properties properties) {
        this.rpcHost = properties.getProperty("RPC_HOST");
        this.rpcPort = (int) properties.get("RPC_PORT");
        this.rpcUser = properties.getProperty("RPC_USER");
        this.rpcPassword = properties.getProperty("RPC_PASSWORD");
    }
}
