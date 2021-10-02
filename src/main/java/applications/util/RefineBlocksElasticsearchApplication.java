package applications.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import elasticsearch.ElasticsearchClientFactory;
import models.BlockHeader;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import utils.LocalDateTimeAdapter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RefineBlocksElasticsearchApplication {

    private final RestHighLevelClient elasticsearchClient;
    private final Gson gson;

    private static final String BITCOIN_BLOCKS_INDEX = "bitcoin-blocks";

    public RefineBlocksElasticsearchApplication(RestHighLevelClient elasticsearchClient, Gson gson) {
        this.elasticsearchClient = elasticsearchClient;
        this.gson = gson;
    }

    public static void main(String[] args) {
        RefineBlocksElasticsearchApplication application = new RefineBlocksElasticsearchApplication(
                ElasticsearchClientFactory.createClient(),
                new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create());
        application.run();
    }

    private void run() {
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));

        SearchRequest searchRequest = new SearchRequest(BITCOIN_BLOCKS_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery()
                .mustNot(QueryBuilders.existsQuery("diffSeconds")));
        searchRequest.source(searchSourceBuilder);
        searchRequest.scroll(scroll);

        try {
            SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

            String scrollId = searchResponse.getScrollId();
            SearchHit[] searchHits = searchResponse.getHits().getHits();

            System.out.printf("processing %d total blocks\n", searchResponse.getHits().getTotalHits().value);

            AtomicInteger blockNum = new AtomicInteger();
            while (searchHits != null && searchHits.length > 0) {

                for (SearchHit hit : searchHits) {
                    BlockHeader blockHeader = gson.fromJson(hit.getSourceAsString(), BlockHeader.class);
                    try {
                        // Genesis Block
                        if (blockHeader.getHeight() == 0L) {
                            continue;
                        }
                        GetResponse getResponse = elasticsearchClient.get(new GetRequest(
                                BITCOIN_BLOCKS_INDEX,
                                blockHeader.getPreviousBlockHash()), RequestOptions.DEFAULT);

                        BlockHeader previousBlockHeader = gson.fromJson(getResponse.getSourceAsString(), BlockHeader.class);
                        long diffSeconds = ChronoUnit.SECONDS.between(previousBlockHeader.getTime(), blockHeader.getTime());
                        long diffMinutes = ChronoUnit.MINUTES.between(previousBlockHeader.getTime(), blockHeader.getTime());
                        long diffHours = ChronoUnit.HOURS.between(previousBlockHeader.getTime(), blockHeader.getTime());

                        UpdateRequest request = new UpdateRequest(BITCOIN_BLOCKS_INDEX, blockHeader.getHash())
                                .doc("diffSeconds", diffSeconds,
                                        "diffMinutes", diffMinutes,
                                        "diffHours", diffHours);
                        elasticsearchClient.updateAsync(request, RequestOptions.DEFAULT, new ActionListener<UpdateResponse>() {
                            @Override
                            public void onResponse(UpdateResponse updateResponse) {
                                System.out.printf("%d) block %s updated\n", blockNum.incrementAndGet(), blockHeader.getHash());
                            }

                            @Override
                            public void onFailure(Exception e) {
                                System.err.printf("Something went wrong with %s\n", blockHeader);
                                e.printStackTrace();
                            }
                        });

                    } catch (IOException e) {
                        System.err.printf("Something went wrong with %s\n", blockHeader);
                        e.printStackTrace();
                    }
                }


                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);
                searchResponse = elasticsearchClient.scroll(scrollRequest, RequestOptions.DEFAULT);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();
            }

            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = elasticsearchClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            boolean succeeded = clearScrollResponse.isSucceeded();

            System.out.println("succeeded" + succeeded);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
