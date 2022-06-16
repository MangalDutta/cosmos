// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.sample.sync;

import com.azure.cosmos.*;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import com.azure.cosmos.models.*;
import com.azure.cosmos.sample.common.AccountSettings;
import com.azure.cosmos.sample.common.Families;
import com.azure.cosmos.sample.common.Family;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Creation of Database/Container using the Java SDK with Service Principal would fail with the below exception
 * The given request [POST /dbs]/[POST /dbs/ToDoList/colls]  cannot be authorized by AAD token in data plane.
 * Learn more: https://aka.ms/cosmos-native-rbac.
 * <p>
 * This permission model covers only database operations that involve reading and writing data. It does not cover
 * any kind of management operations on management resources, for example:
 * <p>
 * Create/Replace/Delete Database
 * Create/Replace/Delete Container
 * Replace Container Throughput
 * Create/Replace/Delete/Read Stored Procedures
 * Create/Replace/Delete/Read Triggers
 * Create/Replace/Delete/Read User Defined Functions
 * <p>
 * NOTE : You cannot use any Azure Cosmos DB data plane SDK to authenticate management operations with an
 * Azure AD identity.
 */
public class ChangeFeedFullFidelity {
    //
    private CosmosAsyncClient client;
    //
    private final String databaseName = "ToDoList";
    private final String containerName = "Items";
    private final String leaseContainerName = AccountSettings.LEASE_NAME;
    //
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private CosmosAsyncContainer leaseContainer;
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    //
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeFeedFullFidelity.class);

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
        ChangeFeedFullFidelity p = new ChangeFeedFullFidelity();
        System.out.println("Start the ChangeFeed Processor ");
        try {
            p.initChangeFeedProcessor();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("ddd", e);
            System.err.println(String.format("Cosmos getStarted failed with %s", e));
        } finally {
            System.out.println("Closing the client");
            p.close();
        }
    }

    public void executeStoredProcedure() throws Exception {
        //
        client = getCosmosClient();
        //
        createDatabaseIfNotExists();
        createContainerIfNotExists();
        //
        String spName = "upsert";
        spName = "create";
        System.out.println(String.format("Executing stored procedure %s...\n\n", spName));

        String partitionValue = "Seattle";
        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
        options.setPartitionKey(new PartitionKey(partitionValue));
        options.setScriptLoggingEnabled(true);

        ArrayList<Family> familiesToCreate = new ArrayList<>();
        for (int i = 1; i <= 750; i++) {
            familiesToCreate.add(Families.getFamily(partitionValue));
        }
        //createFamilies(familiesToCreate);
/*
        familiesToCreate.add(Families.getFamily(partitionValue));
        familiesToCreate.add(Families.getFamily(partitionValue));
        familiesToCreate.add(Families.getFamily(partitionValue));
*/
        //
        List<Object> sproc_args = new ArrayList<>();
        System.out.println(OBJECT_MAPPER.writeValueAsString(familiesToCreate));
        sproc_args.add(familiesToCreate);
//        sproc_args.add(OBJECT_MAPPER.writeValueAsString(familiesToCreate));
//        Object[] storedProcedureArgs = new Object[]{familiesToCreate};

        CosmosStoredProcedureResponse executeResponse = container.getScripts()
                .getStoredProcedure(spName)
                .execute(sproc_args, options).block();

        System.out.println("\n\n------------LOGS = " + executeResponse.getScriptLog() + "\n\n");
        //
        System.out.println(String.format("Stored procedure %s returned %s (HTTP %d), at cost %.3f RU.\n",
                spName,
                executeResponse.getResponseAsString(),
                executeResponse.getStatusCode(),
                executeResponse.getRequestCharge()));
    }

    private void initChangeFeedProcessor() throws Exception {
        System.out.println("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);
        //
        //  Create sync client
        client = getCosmosClient();
        //
        createDatabaseIfNotExists();
        createContainerIfNotExists();
        createLeaseContainerIfNotExists();
        System.out.println("\n\n\n\nCreating materialized view...");
        getChangeFeedProcessor();
        //
    }


    private void getChangeFeedProcessor() {
        //
        CosmosChangeFeedRequestOptions changeFeedRequestOptions = CosmosChangeFeedRequestOptions
                .createForProcessingFromNow(FeedRange.forFullRange())
//                .createForProcessingFromBeginning(FeedRange.forFullRange())
                .fullFidelity();
        //
        FeedResponse itemFeedResponse = container.queryChangeFeed(changeFeedRequestOptions, Object.class)
                .byPage()
                .blockFirst();
        //
        itemFeedResponse.getResults().forEach(e -> System.out.println("e "+e.getClass().getCanonicalName()));

    }


    private void publishMessage() throws Exception {
        System.out.println("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);
        //
        client = getCosmosClient();
        //
        createDatabaseIfNotExists();
        createContainerIfNotExists();
        ArrayList<Family> familiesToCreate = new ArrayList<>();
        familiesToCreate.add(Families.getFamily(AccountSettings.PKEY));
        createFamilies(familiesToCreate);
    }

    private void publishMessages() throws Exception {
        System.out.println("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);
        //
        /*ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(AccountSettings.CLIENT_ID)
                .clientSecret(AccountSettings.CLIENT_SECRET)
                .tenantId(AccountSettings.TENANT_ID)
                .build();*/
        //  Create sync client
        // java -DACCOUNT_HOST=https://albaik.documents.azure.com:443/ -DDATABASE="ToDoList" -DCONTAINER="Items" -DACCOUNT_KEY="oXf30U3jU0hoevJLZP4aymx59fOfvg8fl0eLcbzawQGB98BF7HnOQgX98mum0jeeOxJfcN9K6IS84o3SHxy7yA==" -DWORKER_NAME="Host1"   -jar target\cosmosdb-cfp-uber.jar feed
        client = getCosmosClient();
        //
        createDatabaseIfNotExists();
        createContainerIfNotExists();
        //scaleContainer();
        for (int i = 1; i <= 10; i++) {
            ArrayList<Family> familiesToCreate = new ArrayList<>();
            for (int j = 1; j <= 100; j++) {
                familiesToCreate.add(Families.getFamily());
            }
            createFamilies(familiesToCreate);
        }
        /*ArrayList<Family> familiesToCreate = new ArrayList<>();
            familiesToCreate.add(Families.getFamily());
        createFamilies(familiesToCreate);
        //  Setup family items to create
        ArrayList<Family> familiesToCreate = new ArrayList<>();
        familiesToCreate.add(Families.getAndersenFamilyItem());
        familiesToCreate.add(Families.getWakefieldFamilyItem());
        familiesToCreate.add(Families.getJohnsonFamilyItem());
        familiesToCreate.add(Families.getSmithFamilyItem());
        //
        createFamilies(familiesToCreate);
        //
        System.out.println("Reading items.");
        readItems(familiesToCreate);*/
        //
        //System.out.println("Querying items.");
        //queryItems();
    }

    private CosmosAsyncClient getCosmosClient() {
        ArrayList<String> preferredRegions = new ArrayList<String>();
        preferredRegions.add(AccountSettings.REGION);
        System.out.println("Preferred Region " + AccountSettings.REGION);

        return new CosmosClientBuilder()
                .endpoint(AccountSettings.HOST)
                .preferredRegions(preferredRegions)
                .key(AccountSettings.KEY)
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
        containerSettings.setChangeFeedPolicy(ChangeFeedPolicy.createFullFidelityPolicy(Duration.ofDays(1)));
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

    private void createFamilies(List<Family> families) throws Exception {
        double totalRequestCharge = 0;
        for (Family family : families) {

            //  Create item using container that we created using sync client

            //  Using appropriate partition key improves the performance of database operations
            CosmosItemResponse<Family> item = container.createItem(family,
                            new PartitionKey(family.getPartitionKey()),
                            new CosmosItemRequestOptions())
                    .block();

            //  Get request charge and other properties like latency, and diagnostics strings, etc.
            System.out.println(String.format("Created item with request charge of %.2f within" +
                            " duration %s",
                    item.getRequestCharge(), item.getDuration()));
            totalRequestCharge += item.getRequestCharge();
        }
        System.out.println(String.format("Created %d items with total request " +
                        "charge of %.2f",
                families.size(),
                totalRequestCharge));
    }

    private void readItems(ArrayList<Family> familiesToCreate) {
        //  Using partition key for point read scenarios.
        //  This will help fast look up of items because of partition key
        familiesToCreate.forEach(family -> {
            try {
                CosmosItemResponse<Family> item = container
                        .readItem(family.getId(), new PartitionKey(family.getPartitionKey()), Family.class)
                        .block();
                double requestCharge = item.getRequestCharge();
                Duration requestLatency = item.getDuration();
                System.out.println(String.format("Item successfully read with id %s with a charge of %.2f and within duration %s",
                        item.getItem().getId(), requestCharge, requestLatency));
            } catch (CosmosException e) {
                e.printStackTrace();
                System.err.println(String.format("Read Item failed with %s", e));
            }
        });
    }
/*
    private void queryItems() {
        // Set some common query options
        int preferredPageSize = 10;
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        //  Set populate query metrics to get metrics around query executions
        queryOptions.setQueryMetricsEnabled(true);

        CosmosPagedIterable<Family> familiesPagedIterable =
                container
                        .queryItems(
                "SELECT * FROM Family WHERE Family.partitionKey IN ('Andersen', 'Wakefield', 'Johnson')",
                queryOptions, Family.class);

        familiesPagedIterable.iterableByPage(preferredPageSize).forEach(cosmosItemPropertiesFeedResponse -> {
            System.out.println("Got a page of query result with " +
                    cosmosItemPropertiesFeedResponse.getResults().size() + " items(s)"
                    + " and request charge of " + cosmosItemPropertiesFeedResponse.getRequestCharge());

            System.out.println("Item Ids " + cosmosItemPropertiesFeedResponse
                    .getResults()
                    .stream()
                    .map(Family::getId)
                    .collect(Collectors.toList()));
        });
    }*/
}
