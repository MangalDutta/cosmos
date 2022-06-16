// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package fc.azure.cosmos.cfp;

import com.azure.cosmos.*;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CFPApp {
    //
    private CosmosAsyncClient client;
    //
    private final String databaseName = "ToDoList";
    private final String containerName = "Items";
    private final String leaseContainerName = AccountSettings.CONTAINER + "-lease";
    //
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private CosmosAsyncContainer leaseContainer;
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();
    //
    private static final Logger LOGGER = LoggerFactory.getLogger(CFPApp.class);
    //
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
        CFPApp p = new CFPApp();
        System.out.println("Start the ChangeFeed Processor ");
        try {
            p.initChangeFeedProcessor();
        } catch (Exception e) {
            LOGGER.error("ddd", e);
            System.err.println(String.format("Cosmos getStarted failed with %s", e));
        } finally {
            System.out.println("Closing the client");
            p.close();
        }
    }

    private void initChangeFeedProcessor() throws Exception {
        System.out.println("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);
        //  Create sync client
        client = getCosmosClient();
        //
        createDatabaseIfNotExists();
        createContainerIfNotExists();
        createLeaseContainerIfNotExists();
        System.out.println("\n\n\n\nCreating materialized view...");
        //
        ChangeFeedProcessor changeFeedProcessorInstance = getChangeFeedProcessor("MongoCFP");
        changeFeedProcessorInstance
                .start()
                .subscribeOn(Schedulers.elastic())
                .doOnSuccess(aVoid -> {
                    System.out.println("Successfull");
                    //pass
                })
                .subscribe();
        //
        int counter = 0;
        boolean isRunning = true;
        while (isRunning) {
            List<ChangeFeedProcessorState> states = changeFeedProcessorInstance.getCurrentState().block();
            System.out.println("states size " + states.size());
            for (ChangeFeedProcessorState state : states) {
                System.out.println("ContinuationToken " + state.getContinuationToken() +
                        " LeaseToken " + state.getLeaseToken() +
                        " HostName " + state.getHostName() +
                        " EstimatedLag " + state.getEstimatedLag());
            }
            System.out.println("Sleeping now ");
            Thread.sleep(1000 * 60);
            counter++;
            System.out.println("Awake ");
            if (counter == 500) {
                isRunning = false;
                System.out.println("Exiting ");

            }
        }
    }
/*
    public static ChangeFeedProcessor getChangeFeedProcessor(final String hostName,
                                                             final CosmosAsyncContainer container,
                                                             final CosmosAsyncContainer leaseContainer) {
        ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions();
        options.setStartFromBeginning(true);
        //options.setStartTime(Instant.now().minusSeconds(60*60));
        // If you want multiple consumers to receive same message
        options.setLeasePrefix(RandomStringUtils.randomAlphabetic(5));

        return new ChangeFeedProcessorBuilder()
                .hostName(hostName) // hostName would help you distribute the data across partitions
                .options(options) // options
                .feedContainer(container) // monitored container
                .leaseContainer(leaseContainer) // lease container
                .handleChanges((List<JsonNode> docs) -> {
                    System.out.println("--->setHandleChanges() START");
                    for (JsonNode document : docs) {
                        // Modify the data or persist it as per your need
                    }
                    System.out.println("--->handleChanges() END");
                })
                .buildChangeFeedProcessor();
    }*/

    public static int CHANGE_FEED_COUNTER = 0;

    private ChangeFeedProcessor getChangeFeedProcessor(String hostName) {
        ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions();

        return new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .options(options)
                .feedContainer(container)
                .leaseContainer(leaseContainer)
                .handleChanges((List<JsonNode> docs) -> {
                    System.out.println("--->setHandleChanges() START");
                    //
                    for (JsonNode document : docs) {
                        try {
                            CHANGE_FEED_COUNTER++;
                            System.out.println("---->DOCUMENT RECEIVED: " + OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(document) + " MESSAGE_COUNT " + CHANGE_FEED_COUNTER);
                        } catch (JsonProcessingException e) {
                            LOGGER.error("JsonProcessingException ", e);
                            e.printStackTrace();
                        }
                    }
                    System.out.println("--->handleChanges() END");
                })
                .buildChangeFeedProcessor();
    }


    private CosmosAsyncClient getCosmosClient() {

        return new CosmosClientBuilder()
                .endpoint(AccountSettings.HOST)
                .key(AccountSettings.MASTER_KEY)
                .userAgentSuffix("CosmosDBServiceCFP")
                .consistencyLevel(ConsistencyLevel.SESSION)
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();
    }

    private void createDatabaseIfNotExists() throws Exception {
        System.out.println("Create database " + databaseName + " if not exists.");

        //  Create database if not exists
        client.createDatabaseIfNotExists(databaseName).block();
        database = client.getDatabase(databaseName);
        //
        System.out.println("Checking database " + database.getId() + " completed!\n");
    }

    private void createContainerIfNotExists() throws Exception {
        System.out.println("Create container " + containerName + " if not exists.");
        //
        CosmosAsyncDatabase databaseLink = client.getDatabase(databaseName);
        CosmosAsyncContainer collectionLink = databaseLink.getContainer(containerName);
        CosmosContainerResponse containerResponse = null;

        try {
            containerResponse = collectionLink.read().block();

            if (containerResponse != null) {
                //throw new IllegalArgumentException(String.format("Collection %s already exists in database %s.", collectionName, databaseName));
                container = databaseLink.getContainer(containerName);
                return;
            }
        } catch (RuntimeException ex) {
            if (ex instanceof CosmosException) {
                CosmosException cosmosClientException = (CosmosException) ex;

                if (cosmosClientException.getStatusCode() != 404) {
                    throw ex;
                }
            } else {
                throw ex;
            }
        }
        //
        CosmosContainerProperties containerSettings = new CosmosContainerProperties(containerName, "/partitionKey");
        containerSettings.setDefaultTimeToLiveInSeconds(-1);
        CosmosContainerRequestOptions requestOptions = new CosmosContainerRequestOptions();
        //ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(20000);
        containerResponse = databaseLink.createContainer(containerSettings, requestOptions).block();
        //
        if (containerResponse == null) {
            throw new RuntimeException(String.format("Failed to create collection %s in database %s.", containerName,
                    databaseName));
        }
        //
        container = databaseLink.getContainer(containerName);
        //
        System.out.println("Checking container " + container.getId() + " completed!\n");
    }

    private void createLeaseContainerIfNotExists() throws Exception {
        System.out.println("Create container " + leaseContainerName + " if not exists.");
//
        CosmosAsyncDatabase databaseLink = client.getDatabase(databaseName);
        CosmosAsyncContainer collectionLink = databaseLink.getContainer(leaseContainerName);

        CosmosContainerResponse containerResponse = null;

        try {
            containerResponse = collectionLink.read().block();

            if (containerResponse != null) {
                //throw new IllegalArgumentException(String.format("Collection %s already exists in database %s.", collectionName, databaseName));
                //leaseContainer = databaseLink.getContainer(leaseContainerName);
                //return;
                //collectionLink.delete().block();

                leaseContainer = databaseLink.getContainer(leaseContainerName);
                return;
/*                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }*/
            }
        } catch (RuntimeException ex) {
            if (ex instanceof CosmosException) {
                CosmosException cosmosClientException = (CosmosException) ex;

                if (cosmosClientException.getStatusCode() != 404) {
                    throw ex;
                }
            } else {
                throw ex;
            }
        }
        //
        CosmosContainerProperties containerSettings = new CosmosContainerProperties(leaseContainerName, "/id");
        containerSettings.setDefaultTimeToLiveInSeconds(-1);
        CosmosContainerRequestOptions requestOptions = new CosmosContainerRequestOptions();
        //ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(20000);
        containerResponse = databaseLink.createContainer(containerSettings, requestOptions).block();
        //
        if (containerResponse == null) {
            throw new RuntimeException(String.format("Failed to create collection %s in database %s.", leaseContainerName,
                    databaseName));
        }
        //
        leaseContainer = databaseLink.getContainer(leaseContainerName);
        //
        System.out.println("Checking lease container " + container.getId() + " completed!\n");
    }

}
