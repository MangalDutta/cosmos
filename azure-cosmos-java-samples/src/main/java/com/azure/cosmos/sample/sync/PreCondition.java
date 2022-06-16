package com.azure.cosmos.sample.sync;

import com.azure.cosmos.*;
import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import com.azure.cosmos.models.*;
import com.azure.cosmos.sample.common.AccountSettings;
import com.azure.cosmos.sample.common.Food;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PreCondition {

    //
    private static final Logger LOGGER = LoggerFactory.getLogger(PreCondition.class);
    //
    //private CosmosClient client;
    //
    private final String databaseName = "NutritionData";
    private final String containerName = "Food";
    //
    //private CosmosDatabase database;
    //private CosmosContainer container;

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

    public void close() {
        client.close();
    }

    /**
     * Run a Hello CosmosDB console application.
     *
     * @param args command line args.
     */
    //  <Main>
    public static void main(String[] args) {
        final PreCondition p = new PreCondition();
        CountDownLatch latch = new CountDownLatch(0);

        try {
            p.initialize();
            String uuid = UUID.randomUUID().toString();
            System.out.println("uuid "+uuid);
            //p.readItem();
            String etag = p.addItem(uuid, "precondition");
            final Food food = p.readItem(uuid, "precondition");
            p.updateItem(food, RandomStringUtils.randomAlphabetic(20)+"0001FC", etag);
            p.updateItem(food, RandomStringUtils.randomAlphabetic(20)+"0002FC", "ddd");
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        p.updateItem(food, RandomStringUtils.randomAlphabetic(20), "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            };
            ExecutorService executor = Executors.newFixedThreadPool(1000);
            long max = 1000;
            for(long index=0; index<=max; index++) {
//                executor.submit(run);
            }
            latch.await();

            System.out.println("Demo complete, please hold while resources are released");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(String.format("Cosmos getStarted failed with %s", e));
        } finally {
            System.out.println("Closing the client");
            p.close();
        }
        System.exit(0);
    }

    private void initializeAsync() throws Exception {
        System.out.println("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);
        //
        client = Utils.getAsyncClient();
        database = Utils.createDatabaseIfNotExists(client, databaseName);
        container = Utils.createContainerIfNotExists(containerName, database);
    }

    private void initialize() throws Exception {
        System.out.println("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);
        //
        client = Utils.getAsyncClient();
        database = Utils.createDatabaseIfNotExists(client, databaseName);
        container = Utils.createContainerIfNotExists(containerName, database);
    }

    private String addItem(final String id, final String foodGroup) throws Exception {
        Food food = new Food(id, "hey there", foodGroup);
        CosmosItemResponse response = container.createItem(food).block();
        System.out.println("addItem Request Charge ===> "+response.getRequestCharge() + " etag "+response.getETag());
        return response.getETag();
    }

    private void updateItem(final Food food, final String description, String etag) throws Exception {
        food.setDescription(description);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setIfMatchETag(etag);
        CosmosItemResponse response = container.upsertItem(food, options).block();

        System.out.println("updateItem Request Charge ===> "+response.getRequestCharge() +
                " etag "+response.getETag() +
                " Session Token "+response.getSessionToken());
    }

    private Food readItem(final String id, final String foodGroup) throws Exception {
        CosmosItemResponse<Food> readResponse1 = container.readItem(id,
                new PartitionKey(foodGroup),
                new CosmosItemRequestOptions(),
                Food.class).block();
        readResponse1.getETag();
        System.out.println("Read Request Charge ===> "+readResponse1.getRequestCharge());
        return readResponse1.getItem();
    }

}
