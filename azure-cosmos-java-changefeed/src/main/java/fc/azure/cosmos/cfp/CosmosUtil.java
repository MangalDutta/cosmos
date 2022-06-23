package fc.azure.cosmos.cfp;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;

public class CosmosUtil {

    public static CosmosAsyncClient getCosmosClient(final Config appConfig, final String userAgentSuffix) {
        //
        DirectConnectionConfig config = new DirectConnectionConfig();
        config.setMaxConnectionsPerEndpoint(10);
        config.setMaxRequestsPerConnection(10);
        //
        return new CosmosClientBuilder()
                .endpoint(appConfig.getEndpoint())
                .key(appConfig.getKey())
                .userAgentSuffix(userAgentSuffix)
                .consistencyLevel(appConfig.getReadConsistencyLevel())
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();
    }

    public static CosmosAsyncDatabase createDatabaseIfNotExists(final Config appConfig, final CosmosAsyncClient client) throws Exception {
        System.out.println("Create database " + appConfig.getDatabase() + " if not exists.");
        //  Create database if not exists
        client.createDatabaseIfNotExists(appConfig.getDatabase()).block();
        CosmosAsyncDatabase database = client.getDatabase(appConfig.getDatabase());
        //
        System.out.println("Checking database " + database.getId() + " completed!\n");
        //
        return database;
    }

    public static CosmosAsyncContainer createContainerIfNotExists(final Config appConfig, final CosmosAsyncClient client,
                                            final CosmosAsyncDatabase database) throws Exception {
        System.out.println("Create container " + appConfig.getContainer() + " if not exists.");
        //
        CosmosAsyncDatabase databaseLink = client.getDatabase(appConfig.getDatabase());
        CosmosAsyncContainer collectionLink = databaseLink.getContainer(appConfig.getContainer());
        CosmosContainerResponse containerResponse = null;
        CosmosAsyncContainer container = null;

        try {
            containerResponse = collectionLink.read().block();

            if (containerResponse != null) {
                //throw new IllegalArgumentException(String.format("Collection %s already exists in database %s.", collectionName, databaseName));
                container = databaseLink.getContainer(appConfig.getContainer());
                return container;
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
        CosmosContainerProperties containerSettings = new CosmosContainerProperties(appConfig.getContainer(),
                "/partitionKey");
        containerSettings.setDefaultTimeToLiveInSeconds(-1);
        CosmosContainerRequestOptions requestOptions = new CosmosContainerRequestOptions();
        //ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(20000);
        containerResponse = databaseLink.createContainer(containerSettings, requestOptions).block();
        //
        if (containerResponse == null) {
            throw new RuntimeException(String.format("Failed to create collection %s in database %s.",
                    appConfig.getContainer(),
                    appConfig.getDatabase()));
        }
        //
        container = databaseLink.getContainer(appConfig.getContainer());
        //
        System.out.println("Checking container " + container.getId() + " completed!\n");
        //
        return container;
    }
}
