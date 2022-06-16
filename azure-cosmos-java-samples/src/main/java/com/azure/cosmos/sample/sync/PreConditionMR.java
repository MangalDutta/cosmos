package com.azure.cosmos.sample.sync;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.TriggerOperation;
import com.azure.cosmos.sample.common.AccountSettings;
import com.azure.cosmos.sample.common.Connection;
import com.azure.cosmos.sample.common.ConnectionAsync;
import com.azure.cosmos.sample.common.Food;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class PreConditionMR {

    //
    private static final Logger LOGGER = LoggerFactory.getLogger(PreConditionMR.class);
    //
    //private CosmosClient client;
    //
    private final String databaseName = "NutritionData";
    private final String containerName = "Food";
    //
    //private CosmosDatabase database;
    //private CosmosContainer container;
    //
    private static ConnectionAsync connectionEastUS = new ConnectionAsync();
    //
    private static ConnectionAsync connectionWestUS = new ConnectionAsync();
    //
    private static Connection connection = new Connection();

    /**
     * Run a Hello CosmosDB console application.
     *
     * @param args command line args.
     */
    //  <Main>
    public static void main(String[] args) {
        final PreConditionMR p = new PreConditionMR();
       /* p.mono();
        if (true) {
            System.exit(0);
        }*/
        CountDownLatch latch = new CountDownLatch(0);

        try {
            p.initializeAsync();
            p.initializeSync();
            String uuid = UUID.randomUUID().toString();
            System.out.println("uuid "+uuid);
            //p.readItem();
            //uuid = "00001";
            // connectionWestUS connectionEastUS
            String etag = p.addItem(connectionWestUS.getContainer(), uuid, "precondition");
            System.out.println("etag "+etag);
            final Food food = p.readItem(connectionWestUS.getContainer(), uuid, "precondition");
            p.updateItem(connectionWestUS.getContainer(),
                    food,
                    RandomStringUtils.randomAlphabetic(20)+"0001FC", etag);
            /*p.updateItem(connectionEastUS.getContainer(),
                    food,
                    RandomStringUtils.randomAlphabetic(20)+"0002FC", "ddd");*/
  /*
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
*/
            System.out.println("Demo complete, please hold while resources are released");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(String.format("Cosmos getStarted failed with %s", e));
        } finally {
            System.out.println("Closing the client");
            connectionEastUS.close();
            connectionWestUS.close();
        }
        System.exit(0);
    }

    private void initializeSync() throws Exception {
        System.out.println("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);
        //
        ArrayList<String> preferredRegions1 = new ArrayList<>();
        preferredRegions1.add("East US");
        //
        connection.setClient(Utils.getClient());
        connection.setDatabase(Utils.createDatabaseIfNotExists(connection.getClient(), databaseName));
        connection.setContainer(Utils.createContainerIfNotExists(containerName, connection.getDatabase()));
    }

    private void initializeAsync() throws Exception {
        System.out.println("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);
        //
        ArrayList<String> preferredRegions1 = new ArrayList<>();
        preferredRegions1.add("East US");
        //
        connectionEastUS.setClient(Utils.getAsyncClient(preferredRegions1));
        connectionEastUS.setDatabase(Utils.createDatabaseIfNotExists(connectionEastUS.getClient(), databaseName));
        connectionEastUS.setContainer(Utils.createContainerIfNotExists(containerName, connectionEastUS.getDatabase()));
        //
        ArrayList<String> preferredRegions2 = new ArrayList<>();
        preferredRegions2.add( "West US");
        //
        connectionWestUS.setClient(Utils.getAsyncClient(preferredRegions2));
        connectionWestUS.setDatabase(Utils.createDatabaseIfNotExists(connectionWestUS.getClient(), databaseName));
        connectionWestUS.setContainer(Utils.createContainerIfNotExists(containerName, connectionWestUS.getDatabase()));
    }

    private void mono() {
        //
        LocalDateTime startLocalDateTime = LocalDateTime.now();
        System.out.println("mono enter "+ startLocalDateTime);
        //
        Scheduler singleThread = Schedulers.single();
        Mono<String> missing = Mono
                .just("Friend ")
                .publishOn(singleThread)
                .map(w -> {
                    int random = new Random().nextInt(3);
                    System.out.println("Random "+random);
                    //if(random%2==0) {
                        callme();
                    //}
                    return "Hello " + w + "!!!";
                })
                .timeout(Duration.ofMillis(10))
                .retry(2);
        //missing.concatWith(Mono.just("it").delaySubscription(Duration.ofMinutes(1)));
        /*missing.filter(e -> {
           System.out.println(e);
           return true;
        });*/
        System.out.println("Output ==> "+missing.block());
        //
        LocalDateTime endLocalDateTime = LocalDateTime.now();
        System.out.println("mono exited "+ endLocalDateTime);
    }

    public void callme() throws RuntimeException {
        if(true) {
            //throw new RuntimeException("fdsfdsf");
        }
        try {
            Thread.sleep(60*1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String addItem(final CosmosAsyncContainer container,
                           final String id,
                           final String foodGroup) throws Exception {
        Food food = new Food(id, "hey there", foodGroup);
        CosmosItemResponse response = container
                .createItem(food)
                //.timeout(Duration.ofMillis(10))
                //.retry(2)
                .block();
        System.out.println("addItem Request Charge ===> "+response.getRequestCharge() + " etag "+response.getETag());
        //System.out.println(response.getDiagnostics().toString());
        return response.getETag();
    }

    private void updateItem(final CosmosAsyncContainer container,
                            final Food food,
                            final String description,
                            final String etag) throws Exception {
        System.out.println("updateItem entered ");
        food.setDescription(description);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setIfMatchETag(etag);
        //
        CosmosItemResponse response = container
                .upsertItem(food, options)
                .timeout(Duration.ofMillis(10))
                .retry(10)
                .block();

//        container.createItem()
//        container.upsertItem()
//        container.deleteItem()
        /*
        //CosmosItemResponse response =
        Mono<CosmosItemResponse<Food>> mono = container
                .upsertItem(food, options)
                //.timeout(Duration.ofMillis(1L))
                .doOnNext(x-> {
                    System.out.println("Tag Info ");
                })
                .retry(1);*/
/*
                .onErrorResume(err -> {
                    System.out.println(err);
                    return Mono.empty();
                })
*/
//                .block();

        //CosmosItemResponse response = mono.block();
        if(response!=null)
        System.out.println("updateItem Request Charge ===> "+response.getRequestCharge() +
                " etag "+response.getETag() +
                " Session Token "+response.getSessionToken());
        System.out.println("updateItem exited ");

    }

    private Food readItem(final CosmosAsyncContainer container,
                          final String id,
                          final String foodGroup) throws Exception {
        CosmosItemResponse<Food> readResponse1 = container.readItem(id,
                new PartitionKey(foodGroup),
                new CosmosItemRequestOptions(),
                Food.class).block();
        readResponse1.getETag();
        System.out.println("Read Request Charge ===> "+readResponse1.getRequestCharge());
        return readResponse1.getItem();
    }

}
