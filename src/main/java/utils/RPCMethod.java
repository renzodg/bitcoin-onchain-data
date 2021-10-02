package utils;

public enum RPCMethod {
    GET_BLOCK_HASH("getblockhash"),
    GET_BLOCK_HEADER("getblockheader");

    private final String value;

    RPCMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
