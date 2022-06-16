package com.azure.cosmos.sample.sync;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.sample.common.AccountSettings;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.List;

public class Utils {

    public static CosmosAsyncClient getAsyncClient() {
        CosmosAsyncClient client = new CosmosClientBuilder()
                .endpoint(AccountSettings.HOST)
                .key(AccountSettings.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .consistencyLevel(ConsistencyLevel.SESSION)
                .buildAsyncClient();
        return client;
    }

    public static CosmosAsyncClient getAsyncClient(final List<String> preferredRegions) {
        CosmosAsyncClient client = new CosmosClientBuilder()
                .endpoint(AccountSettings.HOST)
                .key(AccountSettings.MASTER_KEY)
                //.preferredRegions(preferredRegions)
                //.contentResponseOnWriteEnabled(true)
                //.consistencyLevel(ConsistencyLevel.SESSION)
                .buildAsyncClient();
        return client;
    }


    public static CosmosClient getClient() {
        //
        CosmosClient client = null;
        //
        //TODO make this lean
        if (AccountSettings.AADBASEDAUTH) {
            //
            ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                    .clientId(AccountSettings.CLIENT_ID)
                    .clientSecret(AccountSettings.CLIENT_SECRET)
                    .tenantId(AccountSettings.TENANT_ID)
                    .build();
            //  Create sync client
            client = new CosmosClientBuilder()
                    .endpoint(AccountSettings.HOST)
                    .credential(clientSecretCredential)
                    .userAgentSuffix("FC_CosmosDB_Samples")
                    .consistencyLevel(ConsistencyLevel.SESSION)
                    .gatewayMode()
                    .buildClient();
        } else {
            //
            DirectConnectionConfig config = new DirectConnectionConfig();
            //config.setConnectTimeout(Duration.ofMillis(100));
            //config.setNetworkRequestTimeout(Duration.ofMillis(7000));

            //  Create sync client
            System.out.println("Host "+AccountSettings.HOST);
            System.out.println("MASTER_KEY "+AccountSettings.MASTER_KEY);
            client = new CosmosClientBuilder()
                    .endpoint(AccountSettings.HOST)
                    .key(AccountSettings.MASTER_KEY)
                    .userAgentSuffix("FC_CosmosDB_Samples")
                   // .consistencyLevel(ConsistencyLevel.BOUNDED_STALENESS)
                    .directMode(config)
                    //.consistencyLevel(ConsistencyLevel.SESSION)
                    //.gatewayMode()
                    .buildClient();
        }
        //
        return client;
    }

    public static CosmosAsyncDatabase createDatabaseIfNotExists(
            final CosmosAsyncClient client,
            final String databaseName) throws Exception {
        //  Create database if not exists
        //  <CreateDatabaseIfNotExists>
        CosmosAsyncDatabase database;
        Mono<CosmosDatabaseResponse> databaseIfNotExists = client.createDatabaseIfNotExists(databaseName);
        return client.getDatabase(databaseIfNotExists.block().getProperties().getId());
    }

    public static CosmosDatabase createDatabaseIfNotExists(final CosmosClient client,
                                                           final String databaseName) throws Exception {
        System.out.println("Create database " + databaseName + " if not exists.");
        //  Create database if not exists
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(databaseName);
        System.out.println("Get database ");
        CosmosDatabase database = client.getDatabase(databaseResponse.getProperties().getId());
        System.out.println("Checking database " + database.getId() + " completed!\n");
        //
        return database;
    }

    public static CosmosAsyncContainer createContainerIfNotExists(final String containerName,
                                                             final CosmosAsyncDatabase database) throws Exception {
        //  Create container if not exists
        CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerName, "/partitionKey");
        Mono<CosmosContainerResponse> containerIfNotExists = database.createContainerIfNotExists(containerProperties);
        CosmosContainerResponse cosmosContainerResponse = containerIfNotExists.block();
        return database.getContainer(cosmosContainerResponse.getProperties().getId());
    }

    public static CosmosContainer createContainerIfNotExists(final String containerName,
                                                             final CosmosDatabase database) throws Exception {
        System.out.println("Create container " + containerName + " if not exists.");

        //  Create container if not exists
        CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerName, "/partitionKey");

        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties);
        CosmosContainer container = database.getContainer(containerResponse.getProperties().getId());

        System.out.println("Checking container " + container.getId() + " completed!\n");
        //
        return container;
    }

    private void scaleContainer(final String containerName,
                                final CosmosContainer container) throws Exception {
        System.out.println("Scaling container " + containerName + ".");

        try {
            // You can scale the throughput (RU/s) of your container up and down to meet the needs of the workload. Learn more: https://aka.ms/cosmos-request-units
            ThroughputProperties currentThroughput = container.readThroughput().getProperties();
            int newThroughput = currentThroughput.getManualThroughput() + 100;
            container.replaceThroughput(ThroughputProperties.createManualThroughput(newThroughput));
            System.out.println("Scaled container to " + newThroughput + " completed!\n");
        } catch (CosmosException e) {
            if (e.getStatusCode() == 400) {
                System.err.println("Cannot read container throughput.");
                System.err.println(e.getMessage());
            } else {
                throw e;
            }
        }
    }
}
