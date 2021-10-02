package applications;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import elasticsearch.ElasticsearchClientFactory;
import models.BlockHeader;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import rpc.HttpClientFactory;
import rpc.RPCClient;
import rpc.config.RPCConnectionConfig;
import services.BlockchainService;
import utils.LocalDateTimeAdapter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

public class MigrateBlocksToElasticsearchApplication {

    private final RPCConnectionConfig config;
    private final String rpcServer;
    private final RestHighLevelClient elasticsearchClient;
    private final Gson gson;

    private static final int START_HEIGHT_INDEX = 703248;

    public MigrateBlocksToElasticsearchApplication(RPCConnectionConfig config, RestHighLevelClient elasticsearchClient, Gson gson) {
        this.config = config;
        this.rpcServer = String.format("http://%s:%s", config.getRpcHost(), config.getRpcPort());
        this.elasticsearchClient = elasticsearchClient;
        this.gson = gson;
    }

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.put("RPC_HOST", "localhost");
        properties.put("RPC_PORT", 8332);
        properties.put("RPC_USER", "bitcoin");
        properties.put("RPC_PASSWORD", "12345");

        MigrateBlocksToElasticsearchApplication application = buildFrom(properties);
        application.run();
    }

    private static MigrateBlocksToElasticsearchApplication buildFrom(Properties properties) {
        return new MigrateBlocksToElasticsearchApplication(
                new RPCConnectionConfig(properties),
                ElasticsearchClientFactory.createClient(),
                new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create());
    }

    private void run() throws IOException {
        RPCClient rpcClient = new RPCClient(gson, HttpClientFactory.create(config), rpcServer);
        BlockchainService blockchainService = new BlockchainService(rpcClient);

        int currentHeightIndex = START_HEIGHT_INDEX;


        if (currentHeightIndex == 0) {
            // genesis block
            String firstBlockHash = blockchainService.getBlockHash(0);
            BlockHeader currentBlock = blockchainService.getBlockHeader(firstBlockHash);
            IndexRequest request = new IndexRequest("bitcoin-blocks");
            request.id(currentBlock.getHash());
            request.source(gson.toJson(currentBlock), XContentType.JSON);
            elasticsearchClient.indexAsync(request,
                    RequestOptions.DEFAULT,
                    new ActionListener<IndexResponse>() {
                        @Override
                        public void onResponse(IndexResponse indexResponse) {
                            System.out.println("Block stored to elasticsearch: " + currentBlock.getHeight());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            System.err.println("Something went wrong with: " + currentBlock);
                        }
                    });
        }

       String firstBlockHash = blockchainService.getBlockHash(currentHeightIndex);
        BlockHeader currentBlock = blockchainService.getBlockHeader(firstBlockHash);
        BlockHeader previousBlock = currentBlock;

        while (currentBlock.getNextBlockHash() != null && !currentBlock.getNextBlockHash().isBlank()) {
            currentBlock = blockchainService.getBlockHeader(currentBlock.getNextBlockHash());
            long diffSeconds = ChronoUnit.SECONDS.between(previousBlock.getTime(), currentBlock.getTime());
            long diffMinutes = ChronoUnit.MINUTES.between(previousBlock.getTime(), currentBlock.getTime());
            long diffHours = ChronoUnit.HOURS.between(previousBlock.getTime(), currentBlock.getTime());

            IndexRequest request = new IndexRequest("bitcoin-blocks");
            request.id(currentBlock.getHash());
            JsonObject jsonObject = gson.toJsonTree(currentBlock).getAsJsonObject();
            jsonObject.addProperty("diffSeconds", diffSeconds);
            jsonObject.addProperty("diffMinutes", diffMinutes);
            jsonObject.addProperty("diffHours", diffHours);
            request.source(jsonObject.toString(), XContentType.JSON);
            elasticsearchClient.indexAsync(request,
                    RequestOptions.DEFAULT,
                    new ActionListener<>() {
                        @Override
                        public void onResponse(IndexResponse indexResponse) {
                            System.out.println("Block stored to elasticsearch: " + jsonObject.get("height").getAsInt());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            System.err.println("Something went wrong with: " + jsonObject.toString());
                        }
                    });


            previousBlock = currentBlock;
        }
    }
}
