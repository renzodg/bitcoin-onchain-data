package services;

import lombok.RequiredArgsConstructor;
import rpc.RPCClient;
import rpc.RPCRequest;
import models.BlockHeader;
import utils.RPCMethod;

import java.io.IOException;
import java.util.UUID;

import static rpc.RPCClient.params;

@RequiredArgsConstructor
public class BlockchainService {

    private final RPCClient rpcClient;

    public String getBlockHash(int heightIndex) throws IOException {
        return rpcClient.invokeRPC(
                new RPCRequest(UUID.randomUUID().toString(), RPCMethod.GET_BLOCK_HASH.getValue(), params(heightIndex)),
                String.class).getResult();
    }

    public BlockHeader getBlockHeader(String blockHash) throws IOException {
        return rpcClient.invokeRPC(
                new RPCRequest(UUID.randomUUID().toString(), RPCMethod.GET_BLOCK_HEADER.getValue(), params(blockHash)),
                BlockHeader.class).getResult();
    }
}
